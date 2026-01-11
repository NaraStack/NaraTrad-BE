package com.naratrad.service;

import com.naratrad.entity.ApiLog;
import com.naratrad.repository.ApiLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;

@Service
public class StockService {

    @Autowired
    private ApiLogRepository apiLogRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String FINNHUB_URL = "https://finnhub.io/api/v1/quote?symbol=";
    private final String API_KEY = "YOUR_FINNHUB_KEY";

    public Double getStockPrice(String symbol) {
        // 1. Mulai hitung waktu (Start Time)
        long startTime = System.currentTimeMillis();

        try {
            // 2. Proses panggil API Finnhub
            String url = FINNHUB_URL + symbol + "&token=" + API_KEY;
            // Anggap kita mengambil harga dari response Finnhub
            Double price = restTemplate.getForObject(url, Double.class);

            // 3. Hitung durasi (Duration)
            long duration = System.currentTimeMillis() - startTime;

            // 4. Simpan log ke database secara otomatis
            saveLog("/quote", duration);

            return price;
        } catch (Exception e) {
            // Tetap catat log meskipun error untuk melihat failure rate
            long duration = System.currentTimeMillis() - startTime;
            saveLog("/quote-error", duration);
            throw new RuntimeException("Gagal mengambil data saham");
        }
    }

    // Method helper agar kode lebih rapi
    private void saveLog(String endpoint, long duration) {
        ApiLog log = ApiLog.builder()
                .endpoint(endpoint)
                .responseTimeMs(duration)
                .timestamp(LocalDateTime.now())
                .build();
        apiLogRepository.save(log);
    }
}