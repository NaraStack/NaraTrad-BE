package com.naratrad.service;

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
     * READ: Mengambil data dari database dan menggabungkannya dengan harga real-time.
     * Menggunakan try-catch agar jika satu API call gagal, aplikasi tidak Error 500.
     */
    public List<PortfolioResponseDTO> getFullPortfolio() {
        List<Portfolio> myStocks = repository.findAll();

        return myStocks.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private PortfolioResponseDTO convertToDto(Portfolio stock) {
        PortfolioResponseDTO dto = new PortfolioResponseDTO();
        dto.setId(stock.getId());
        dto.setSymbol(stock.getSymbol());
        dto.setQuantity(stock.getQuantity());

        try {
            String url = String.format(FINNHUB_URL, stock.getSymbol(), apiKey);
            // Mengambil response sebagai Map raw
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.get("c") != null) {
                // Menggunakan Number untuk menangani konversi Integer/Double secara aman
                double currentPrice = ((Number) response.get("c")).doubleValue();
                double percentChange = ((Number) response.get("dp")).doubleValue();

                dto.setPrice(currentPrice);
                dto.setChange(percentChange);
                dto.setTotalValue(currentPrice * stock.getQuantity());
            } else {
                setEmptyPriceData(dto);
            }
        } catch (Exception e) {
            // Log error di console agar bisa didebug tanpa menghentikan aplikasi
            System.err.println("Gagal fetch Finnhub untuk " + stock.getSymbol() + ": " + e.getMessage());
            setEmptyPriceData(dto);
        }
        return dto;
    }

    private void setEmptyPriceData(PortfolioResponseDTO dto) {
        dto.setPrice(0.0);
        dto.setChange(0.0);
        dto.setTotalValue(0.0);
    }

    /**
     * CREATE / UPDATE: Menambah stok baru.
     * Jika simbol sudah ada, jumlah (quantity) akan ditambahkan ke data lama.
     */
    public Portfolio addOrUpdateStock(Portfolio stock) {
        // Memastikan simbol selalu Uppercase untuk konsistensi di DB dan API
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
     * DELETE: Menghapus data stok berdasarkan ID primary key.
     */
    public void deleteStock(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        }
    }

    public PortfolioSummaryDTO getSummary() {
        // 1. Ambil semua data detail (yang sudah ada harganya dari Finnhub)
        List<PortfolioResponseDTO> allDetails = getFullPortfolio();

        // 2. Hitung total nilai seluruh portofolio
        Double totalValue = allDetails.stream()
                .mapToDouble(PortfolioResponseDTO::getTotalValue)
                .sum();

        // 3. Masukkan ke dalam DTO Summary
        PortfolioSummaryDTO summary = new PortfolioSummaryDTO();
        summary.setTotalPortfolioValue(totalValue);
        summary.setTotalStocksOwned(allDetails.size());

        return summary;
    }
}