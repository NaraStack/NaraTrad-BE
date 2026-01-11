package com.naratrad.service;

import com.naratrad.dto.DashboardDTO;
import com.naratrad.dto.PerformanceChartDTO;
import com.naratrad.dto.PortfolioResponseDTO;
import com.naratrad.dto.PortfolioSummaryDTO;
import com.naratrad.entity.Portfolio;
import com.naratrad.entity.User;
import com.naratrad.repository.PortfolioRepository;
import com.naratrad.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.naratrad.repository.ApiLogRepository apiLogRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${finnhub.api.key}")
    private String apiKey;

    private final String FINNHUB_URL = "https://finnhub.io/api/v1/quote?symbol=%s&token=%s";

    /**
     * READ: Fetch all stock data from DB and combine it with real-time prices
     * SECURITY FIX: Filter by user email from JWT token
     */
    public List<PortfolioResponseDTO> getFullPortfolio(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        List<Portfolio> myStocks = repository.findByUser(user);
        return myStocks.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * Main logic for Entity to DTO mapping and Finnhub API integration
     */
    private PortfolioResponseDTO convertToDto(Portfolio stock) {
        PortfolioResponseDTO dto = new PortfolioResponseDTO();
        dto.setId(stock.getId());
        dto.setSymbol(stock.getSymbol());
        dto.setQuantity(stock.getQuantity());
        dto.setPurchasePrice(stock.getPurchasePrice());
        dto.setCreatedAt(stock.getCreatedAt());

        // 1. Mulai hitung waktu tepat sebelum panggil API
        long startTime = System.currentTimeMillis();

        try {
            String url = String.format(FINNHUB_URL, stock.getSymbol(), apiKey);

            // Eksekusi pemanggilan API Finnhub
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            // 2. Hitung durasi setelah response didapat
            long duration = System.currentTimeMillis() - startTime;

            if (response != null && response.get("c") != null) {
                // 3. Simpan log sukses ke database
                saveApiLog("/api/v1/quote", duration);

                // Logic perhitungan harga kamu tetap sama...
                double currentPrice = ((Number) response.get("c")).doubleValue();
                double percentChange = ((Number) response.get("dp")).doubleValue();
                double prevClose = ((Number) response.get("pc")).doubleValue();
                double diff = ((Number) response.get("d")).doubleValue();

                dto.setPrice(currentPrice);
                dto.setChange(percentChange);
                dto.setPreviousClose(prevClose);
                dto.setPriceChange(diff);
                dto.setTotalValue(currentPrice * stock.getQuantity());

                double totalInvestment = stock.getPurchasePrice() * stock.getQuantity();
                double gainLoss = (currentPrice - stock.getPurchasePrice()) * stock.getQuantity();
                double gainLossPercent = ((currentPrice - stock.getPurchasePrice()) / stock.getPurchasePrice()) * 100;

                dto.setTotalInvestment(totalInvestment);
                dto.setGainLoss(gainLoss);
                dto.setGainLossPercent(gainLossPercent);
            } else {
                // Simpan log jika response kosong/null
                saveApiLog("/api/v1/quote-empty", duration);
                setEmptyPriceData(dto);
            }
        } catch (Exception e) {
            // 4. Hitung durasi dan simpan log jika terjadi Error (Network timeout, dll)
            long duration = System.currentTimeMillis() - startTime;
            saveApiLog("/api/v1/quote-error", duration);

            System.err.println("Failed to fetch Finnhub for " + stock.getSymbol() + ": " + e.getMessage());
            setEmptyPriceData(dto);
        }
        return dto;
    }

    /**
     * Helper method untuk simpan ke Repository (Pastikan apiLogRepository sudah di-@Autowired di atas)
     */
    private void saveApiLog(String endpoint, long duration) {
        com.naratrad.entity.ApiLog log = com.naratrad.entity.ApiLog.builder()
                .endpoint(endpoint)
                .responseTimeMs(duration)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        apiLogRepository.save(log);
    }
    /**
     * Set default values when price data is unavailable
     */
    private void setEmptyPriceData(PortfolioResponseDTO dto) {
        dto.setPrice(0.0);
        dto.setChange(0.0);
        dto.setPreviousClose(0.0);
        dto.setPriceChange(0.0);
        dto.setTotalValue(0.0);
        dto.setTotalInvestment(0.0);
        dto.setGainLoss(0.0);
        dto.setGainLossPercent(0.0);
    }

    /**
     * CREATE / UPDATE: Add stock with uppercase symbol enforcement.
     * If stock exists, calculate weighted average price.
     * If purchase price is not provided, auto-fetch current price.
     * SECURITY FIX: Link portfolio to user and verify by user+symbol
     */
    public Portfolio addOrUpdateStock(String email, Portfolio stock) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String symbol = stock.getSymbol().toUpperCase();

        // Auto-set purchasePrice if not filled (null or 0)
        if (stock.getPurchasePrice() == null || stock.getPurchasePrice() == 0.0) {
            Double currentPrice = getLivePrice(symbol);
            if (currentPrice == null || currentPrice == 0.0) {
                throw new RuntimeException("Failed to get price for symbol: " + symbol);
            }
            stock.setPurchasePrice(currentPrice);
        }

        // SECURITY FIX: Check by user AND symbol, not only symbol
        Optional<Portfolio> existingStock = repository.findByUserAndSymbol(user, symbol);

        if (existingStock.isPresent()) {
            Portfolio p = existingStock.get();

            // Calculate weighted average price
            double existingTotalCost = p.getPurchasePrice() * p.getQuantity();
            double newTotalCost = stock.getPurchasePrice() * stock.getQuantity();
            int totalQuantity = p.getQuantity() + stock.getQuantity();
            double weightedAvgPrice = (existingTotalCost + newTotalCost) / totalQuantity;

            p.setQuantity(totalQuantity);
            p.setPurchasePrice(weightedAvgPrice);
            return repository.save(p);
        }

        // Link portfolio ke user
        stock.setUser(user);
        stock.setSymbol(symbol);
        return repository.save(stock);
    }

    /**
     * DELETE: Remove stock by ID
     * SECURITY FIX: Verify ownership before deletion
     */
    public void deleteStock(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify ownership: can only delete own portfolio
        Portfolio portfolio = repository.findByUserAndId(user, id)
                .orElseThrow(() -> new RuntimeException("Portfolio not found or not owned by you"));

        repository.delete(portfolio);
    }

    /**
     * Get portfolio summary only
     * SECURITY FIX: Filter by user
     */
    public PortfolioSummaryDTO getSummary(String email) {
        List<PortfolioResponseDTO> allDetails = getFullPortfolio(email);

        Double totalValue = allDetails.stream()
                .mapToDouble(PortfolioResponseDTO::getTotalValue)
                .sum();

        PortfolioSummaryDTO summary = new PortfolioSummaryDTO();
        summary.setTotalPortfolioValue(totalValue);
        summary.setTotalStocksOwned(allDetails.size());

        return summary;
    }

    /**
     * Get complete dashboard data (Summary + Stock List)
     * SECURITY FIX: Filter by user
     */
    public DashboardDTO getDashboardData(String email) {
        List<PortfolioResponseDTO> allDetails = getFullPortfolio(email);

        // Calculate total portfolio value (sum of all current values)
        Double totalValue = allDetails.stream()
                .mapToDouble(PortfolioResponseDTO::getTotalValue)
                .sum();

        // Calculate total investment (sum of all purchase costs)
        Double totalInvestment = allDetails.stream()
                .mapToDouble(PortfolioResponseDTO::getTotalInvestment)
                .sum();

        // Calculate total gain/loss since purchase
        Double totalGainLoss = allDetails.stream()
                .mapToDouble(PortfolioResponseDTO::getGainLoss)
                .sum();

        // Calculate daily change (today's price change * quantity for all stocks)
        Double dailyChange = allDetails.stream()
                .mapToDouble(stock -> {
                    Double priceChange = stock.getPriceChange() != null ? stock.getPriceChange() : 0.0;
                    return priceChange * stock.getQuantity();
                })
                .sum();

        // Calculate previous total value (yesterday's closing value)
        Double previousTotalValue = allDetails.stream()
                .mapToDouble(stock -> {
                    Double prevClose = stock.getPreviousClose() != null ? stock.getPreviousClose() : 0.0;
                    return prevClose * stock.getQuantity();
                })
                .sum();

        // ROI = (Total Gain/Loss / Total Investment) * 100
        Double roi = totalInvestment > 0 ? (totalGainLoss / totalInvestment) * 100 : 0.0;

        // Daily Change Percent = (Daily Change / Previous Total Value) * 100
        Double dailyChangePercent = previousTotalValue > 0 ? (dailyChange / previousTotalValue) * 100 : 0.0;

        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setTotalPortfolioValue(totalValue);
        dashboard.setTotalStocksOwned(allDetails.size());
        dashboard.setTotalInvestment(totalInvestment);
        dashboard.setTotalGainLoss(totalGainLoss);
        dashboard.setRoi(roi);
        dashboard.setDailyChange(dailyChange);
        dashboard.setDailyChangePercent(dailyChangePercent);
        dashboard.setStockList(allDetails);

        return dashboard;
    }

    /**
     * Search for stock symbols
     * Returns hardcoded list of popular stocks filtered by query
     */
    public List<Map<String, String>> searchSymbols(String query) {
        // Hardcoded popular stock symbols
        List<Map<String, String>> mockSymbols = List.of(
                Map.of("symbol", "AAPL", "name", "Apple Inc."),
                Map.of("symbol", "MSFT", "name", "Microsoft Corp."),
                Map.of("symbol", "GOOGL", "name", "Alphabet Inc."),
                Map.of("symbol", "AMZN", "name", "Amazon.com Inc."),
                Map.of("symbol", "TSLA", "name", "Tesla Inc."),
                Map.of("symbol", "META", "name", "Meta Platforms Inc."),
                Map.of("symbol", "NVDA", "name", "NVIDIA Corp."),
                Map.of("symbol", "NFLX", "name", "Netflix Inc."),
                Map.of("symbol", "PYPL", "name", "PayPal Holdings"),
                Map.of("symbol", "ADBE", "name", "Adobe Inc."),
                Map.of("symbol", "DIS", "name", "Walt Disney Co."),
                Map.of("symbol", "NKE", "name", "Nike Inc."),
                Map.of("symbol", "SBUX", "name", "Starbucks Corp."),
                Map.of("symbol", "TSM", "name", "Taiwan Semiconductor"),
                Map.of("symbol", "BABA", "name", "Alibaba Group"),
                Map.of("symbol", "V", "name", "Visa Inc."),
                Map.of("symbol", "MA", "name", "Mastercard Inc."),
                Map.of("symbol", "JPM", "name", "JPMorgan Chase"),
                Map.of("symbol", "BAC", "name", "Bank of America"),
                Map.of("symbol", "WMT", "name", "Walmart Inc."),
                Map.of("symbol", "KO", "name", "Coca-Cola Co."),
                Map.of("symbol", "PEP", "name", "PepsiCo Inc."),
                Map.of("symbol", "COST", "name", "Costco Wholesale"),
                Map.of("symbol", "AMD", "name", "Advanced Micro Devices"),
                Map.of("symbol", "INTC", "name", "Intel Corp."),
                Map.of("symbol", "ORCL", "name", "Oracle Corp."),
                Map.of("symbol", "CRM", "name", "Salesforce Inc."),
                Map.of("symbol", "CSCO", "name", "Cisco Systems"),
                Map.of("symbol", "TMUS", "name", "T-Mobile US"),
                Map.of("symbol", "VZ", "name", "Verizon Communications"),
                Map.of("symbol", "T", "name", "AT&T Inc."),
                Map.of("symbol", "IBM", "name", "IBM Corp."),
                Map.of("symbol", "GE", "name", "General Electric"),
                Map.of("symbol", "F", "name", "Ford Motor Co."),
                Map.of("symbol", "GM", "name", "General Motors"),
                Map.of("symbol", "UBER", "name", "Uber Technologies"),
                Map.of("symbol", "ABNB", "name", "Airbnb Inc."),
                Map.of("symbol", "SPOT", "name", "Spotify Technology"),
                Map.of("symbol", "SQ", "name", "Block Inc."),
                Map.of("symbol", "SHOP", "name", "Shopify Inc."),
                Map.of("symbol", "ZM", "name", "Zoom Video"),
                Map.of("symbol", "PLTR", "name", "Palantir Technologies"),
                Map.of("symbol", "RIVN", "name", "Rivian Automotive"),
                Map.of("symbol", "LCID", "name", "Lucid Group"),
                Map.of("symbol", "PFE", "name", "Pfizer Inc."),
                Map.of("symbol", "MRNA", "name", "Moderna Inc."),
                Map.of("symbol", "JNJ", "name", "Johnson & Johnson"),
                Map.of("symbol", "XOM", "name", "Exxon Mobil"),
                Map.of("symbol", "CVX", "name", "Chevron Corp."),
                Map.of("symbol", "TM", "name", "Toyota Motor")
        );

        // Filter based on user input
        String upperQuery = query.toUpperCase();
        return mockSymbols.stream()
                .filter(s -> s.get("symbol").contains(upperQuery) ||
                        s.get("name").toUpperCase().contains(upperQuery))
                .limit(10) // Limit to 10 results for clean UI
                .collect(Collectors.toList());
    }

    /**
     * Get live price for a single symbol
     */
    public Double getLivePrice(String symbol) {
        long startTime = System.currentTimeMillis(); // CATAT WAKTU MULAI
        try {
            String url = String.format(FINNHUB_URL, symbol.toUpperCase(), apiKey);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            long duration = System.currentTimeMillis() - startTime;
            saveApiLog("/api/v1/quote-live", duration); // SIMPAN LOG

            if (response != null && response.get("c") != null) {
                return ((Number) response.get("c")).doubleValue();
            }
        } catch (Exception e) {
            saveApiLog("/api/v1/quote-live-error", System.currentTimeMillis() - startTime);
            System.err.println("Error fetching price: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Calculate total value (Live Calculation for Add Stock form)
     */
    public Map<String, Object> calculateTotalValue(String symbol, Integer quantity) {
        Double price = getLivePrice(symbol);
        Double totalValue = price * quantity;

        return Map.of(
                "symbol", symbol.toUpperCase(),
                "quantity", quantity,
                "currentPrice", price,
                "totalValue", totalValue
        );
    }

    /**
     * Get portfolio performance data for the last 7 days
     * SECURITY FIX: Filter by user
     * NOTE: This generates simulated data based on current portfolio value and gain/loss trend.
     * For production, implement actual historical tracking.
     */
    public PerformanceChartDTO getPerformanceChart(String email) {
        // Get current dashboard data
        DashboardDTO dashboard = getDashboardData(email);

        Double currentValue = dashboard.getTotalPortfolioValue();
        Double totalInvestment = dashboard.getTotalInvestment();
        Double totalGainLoss = dashboard.getTotalGainLoss();

        // If portfolio is empty, return empty chart
        if (currentValue == 0 || totalInvestment == 0) {
            return new PerformanceChartDTO(List.of(), List.of());
        }

        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        LocalDate today = LocalDate.now();

        // Generate data for the last 7 days
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            labels.add(date.format(formatter));

            // Simulate gradual progression from investment to current value
            // Day 0 (6 days ago) starts closer to investment, Day 6 (today) is current value
            double progress = (6.0 - i) / 6.0; // 0.0 to 1.0

            // Calculate simulated value for this day
            // Value = Investment + (GainLoss * progress)
            double simulatedValue = totalInvestment + (totalGainLoss * progress);

            // Add slight random variation for realism (±0.5% of the value)
            double variation = simulatedValue * (Math.random() * 0.01 - 0.005);
            double finalValue = Math.max(0, simulatedValue + variation);

            // Round to 2 decimal places
            values.add(Math.round(finalValue * 100.0) / 100.0);
        }

        return new PerformanceChartDTO(labels, values);
    }
}