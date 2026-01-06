package com.naratrad.service;

import com.naratrad.dto.DashboardDTO;
import com.naratrad.dto.PortfolioResponseDTO;
import com.naratrad.dto.PortfolioSummaryDTO;
import com.naratrad.entity.Portfolio;
import com.naratrad.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${finnhub.api.key}")
    private String apiKey;

    private final String FINNHUB_URL = "https://finnhub.io/api/v1/quote?symbol=%s&token=%s";

    /**
     * READ: Mengambil semua data stok dari DB dan menggabungkannya dengan harga real-time
     */
    public List<PortfolioResponseDTO> getFullPortfolio() {
        List<Portfolio> myStocks = repository.findAll();
        return myStocks.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * Logika utama untuk mapping Entity ke DTO dan integrasi API Finnhub
     */
    private PortfolioResponseDTO convertToDto(Portfolio stock) {
        PortfolioResponseDTO dto = new PortfolioResponseDTO();
        dto.setId(stock.getId());
        dto.setSymbol(stock.getSymbol());
        dto.setQuantity(stock.getQuantity());
        dto.setPurchasePrice(stock.getPurchasePrice());
        dto.setCreatedAt(stock.getCreatedAt());

        try {
            String url = String.format(FINNHUB_URL, stock.getSymbol(), apiKey);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.get("c") != null) {
                // Mengambil data harga saat ini dan perubahan persen
                double currentPrice = ((Number) response.get("c")).doubleValue();
                double percentChange = ((Number) response.get("dp")).doubleValue();

                // Historical Performance: Harga penutupan kemarin (pc) & selisih harga (d)
                double prevClose = ((Number) response.get("pc")).doubleValue();
                double diff = ((Number) response.get("d")).doubleValue();

                dto.setPrice(currentPrice);
                dto.setChange(percentChange);
                dto.setPreviousClose(prevClose);
                dto.setPriceChange(diff);
                dto.setTotalValue(currentPrice * stock.getQuantity());

                // Calculate investment & gain/loss
                double totalInvestment = stock.getPurchasePrice() * stock.getQuantity();
                double gainLoss = (currentPrice - stock.getPurchasePrice()) * stock.getQuantity();
                double gainLossPercent = ((currentPrice - stock.getPurchasePrice()) / stock.getPurchasePrice()) * 100;

                dto.setTotalInvestment(totalInvestment);
                dto.setGainLoss(gainLoss);
                dto.setGainLossPercent(gainLossPercent);
            } else {
                setEmptyPriceData(dto);
            }
        } catch (Exception e) {
            System.err.println("Gagal fetch Finnhub untuk " + stock.getSymbol() + ": " + e.getMessage());
            setEmptyPriceData(dto);
        }
        return dto;
    }

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
     * CREATE / UPDATE: Menambah stok. Simbol dipaksa Uppercase.
     * Jika stok sudah ada, hitung weighted average price.
     * Jika purchasePrice tidak diisi, otomatis gunakan current price.
     */
    public Portfolio addOrUpdateStock(Portfolio stock) {
        String symbol = stock.getSymbol().toUpperCase();

        // Auto-set purchasePrice jika tidak diisi (null atau 0)
        if (stock.getPurchasePrice() == null || stock.getPurchasePrice() == 0.0) {
            Double currentPrice = getLivePrice(symbol);
            if (currentPrice == null || currentPrice == 0.0) {
                throw new RuntimeException("Gagal mendapatkan harga untuk symbol: " + symbol);
            }
            stock.setPurchasePrice(currentPrice);
        }

        Optional<Portfolio> existingStock = repository.findBySymbol(symbol);

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

        stock.setSymbol(symbol);
        return repository.save(stock);
    }

    /**
     * DELETE: Menghapus data berdasarkan ID
     */
    public void deleteStock(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        }
    }

    /**
     * Mendapatkan Ringkasan Portofolio saja
     */
    public PortfolioSummaryDTO getSummary() {
        List<PortfolioResponseDTO> allDetails = getFullPortfolio();

        Double totalValue = allDetails.stream()
                .mapToDouble(PortfolioResponseDTO::getTotalValue)
                .sum();

        PortfolioSummaryDTO summary = new PortfolioSummaryDTO();
        summary.setTotalPortfolioValue(totalValue);
        summary.setTotalStocksOwned(allDetails.size());

        return summary;
    }

    /**
     * Mendapatkan Paket Lengkap untuk Dashboard (Summary + List Stok)
     */
    public DashboardDTO getDashboardData() {
        List<PortfolioResponseDTO> allDetails = getFullPortfolio();

        Double totalValue = allDetails.stream()
                .mapToDouble(PortfolioResponseDTO::getTotalValue)
                .sum();

        Double totalInvestment = allDetails.stream()
                .mapToDouble(PortfolioResponseDTO::getTotalInvestment)
                .sum();

        Double totalGainLoss = allDetails.stream()
                .mapToDouble(PortfolioResponseDTO::getGainLoss)
                .sum();

        // ROI = (Total Gain/Loss / Total Investment) * 100
        Double roi = totalInvestment > 0 ? (totalGainLoss / totalInvestment) * 100 : 0.0;

        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setTotalPortfolioValue(totalValue);
        dashboard.setTotalStocksOwned(allDetails.size());
        dashboard.setTotalInvestment(totalInvestment);
        dashboard.setTotalGainLoss(totalGainLoss);
        dashboard.setRoi(roi);
        dashboard.setStockList(allDetails);

        return dashboard;
    }

    public List<Map<String, String>> searchSymbols(String query) {
        // 1. Hardcode 50 data simbol populer
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

        // 2. Filter berdasarkan input user
        String upperQuery = query.toUpperCase();
        return mockSymbols.stream()
                .filter(s -> s.get("symbol").contains(upperQuery) ||
                        s.get("name").toUpperCase().contains(upperQuery))
                .limit(10) // Tetap batasi 10 hasil agar rapi di UI
                .collect(Collectors.toList());
    }

    // Method untuk ambil harga satuan saja
    public Double getLivePrice(String symbol) {
        try {
            String url = String.format(FINNHUB_URL, symbol.toUpperCase(), apiKey);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.get("c") != null) {
                return ((Number) response.get("c")).doubleValue();
            }
        } catch (Exception e) {
            System.err.println("Error fetching price: " + e.getMessage());
        }
        return 0.0;
    }

    // Method untuk hitung total value (Live Calculation)
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
}