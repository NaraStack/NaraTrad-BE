package com.naratrad.service;

import com.naratrad.dto.WatchlistRequestDTO;
import com.naratrad.dto.WatchlistResponseDTO;
import com.naratrad.dto.WatchlistUpdateDTO;
import com.naratrad.entity.User;
import com.naratrad.entity.Watchlist;
import com.naratrad.repository.UserRepository;
import com.naratrad.repository.WatchlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WatchlistService {

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${finnhub.api.key}")
    private String apiKey;

    private final String FINNHUB_URL = "https://finnhub.io/api/v1/quote?symbol=%s&token=%s";

    /**
     * Get all watchlist items for the authenticated user with real-time prices
     */
    public List<WatchlistResponseDTO> getAllWatchlist(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        List<Watchlist> watchlistItems = watchlistRepository.findByUser(user);
        return watchlistItems.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * Add a symbol to the watchlist
     */
    public WatchlistResponseDTO addToWatchlist(String email, WatchlistRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if symbol already exists in watchlist
        if (watchlistRepository.existsByUserAndSymbol(user, dto.getSymbol().toUpperCase())) {
            throw new RuntimeException("Symbol " + dto.getSymbol() + " is already on your watchlist");
        }

        // Create new watchlist item
        Watchlist watchlist = Watchlist.builder()
                .user(user)
                .symbol(dto.getSymbol().toUpperCase())
                .targetPrice(dto.getTargetPrice())
                .build();

        Watchlist saved = watchlistRepository.save(watchlist);
        return convertToDto(saved);
    }

    /**
     * Remove a symbol from the watchlist
     */
    public void removeFromWatchlist(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Watchlist watchlist = watchlistRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new RuntimeException("Watchlist item not found or not yours"));

        watchlistRepository.delete(watchlist);
    }

    /**
     * Convert Watchlist entity to DTO with real-time price data from Finnhub
     */
    private WatchlistResponseDTO convertToDto(Watchlist watchlist) {
        WatchlistResponseDTO dto = WatchlistResponseDTO.builder()
                .id(watchlist.getId())
                .symbol(watchlist.getSymbol())
                .targetPrice(watchlist.getTargetPrice())
                .createdAt(watchlist.getCreatedAt() != null ?
                        watchlist.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null)
                .build();

        try {
            String url = String.format(FINNHUB_URL, watchlist.getSymbol(), apiKey);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.get("c") != null) {
                double currentPrice = ((Number) response.get("c")).doubleValue();
                double percentChange = ((Number) response.get("dp")).doubleValue();
                double priceChange = ((Number) response.get("d")).doubleValue();

                // Volume might be null for some symbols
                Long volume = null;
                if (response.get("v") != null) {
                    volume = ((Number) response.get("v")).longValue();
                }

                dto.setPrice(currentPrice);
                dto.setChange(percentChange);
                dto.setPriceChange(priceChange);
                dto.setVolume(volume);
            } else {
                setEmptyPriceData(dto);
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch Finnhub for " + watchlist.getSymbol() + ": " + e.getMessage());
            setEmptyPriceData(dto);
        }

        return dto;
    }

    /**
     * Set default values when Finnhub API fails
     */
    private void setEmptyPriceData(WatchlistResponseDTO dto) {
        dto.setPrice(0.0);
        dto.setChange(0.0);
        dto.setPriceChange(0.0);
        dto.setVolume(0L);
    }

    /**
     * Update target price by symbol with data from Request Body
     */
    public WatchlistResponseDTO updateTargetPriceBySymbol(String email, String symbol, WatchlistUpdateDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Watchlist watchlist = watchlistRepository.findByUserAndSymbol(user, symbol.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Symbol " + symbol + " tidak ditemukan di watchlist kamu"));

        watchlist.setTargetPrice(dto.getTargetPrice());

        Watchlist saved = watchlistRepository.save(watchlist);
        return convertToDto(saved);
    }
}