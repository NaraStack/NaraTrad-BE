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
    }

    /**
     * CREATE / UPDATE: Menambah stok. Simbol dipaksa Uppercase.
     */
    public Portfolio addOrUpdateStock(Portfolio stock) {
        String symbol = stock.getSymbol().toUpperCase();
        Optional<Portfolio> existingStock = repository.findBySymbol(symbol);

        if (existingStock.isPresent()) {
            Portfolio p = existingStock.get();
            p.setQuantity(p.getQuantity() + stock.getQuantity());
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

        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setTotalPortfolioValue(totalValue);
        dashboard.setTotalStocksOwned(allDetails.size());
        dashboard.setStockList(allDetails);

        return dashboard;
    }
}