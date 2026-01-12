package com.naratrad.controller;

import com.naratrad.dto.SuccessResponse;
import com.naratrad.dto.WatchlistRequestDTO;
import com.naratrad.dto.WatchlistResponseDTO;
import com.naratrad.dto.WatchlistUpdateDTO;
import com.naratrad.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
@Tag(name = "Watchlist Management", description = "Manage user watchlist with real-time stock prices")
public class WatchlistController {

    @Autowired
    private WatchlistService watchlistService;

    @GetMapping
    @Operation(summary = "Get all watchlist items with real-time prices")
    public ResponseEntity<List<WatchlistResponseDTO>> getAllWatchlist() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<WatchlistResponseDTO> watchlist = watchlistService.getAllWatchlist(email);
        return ResponseEntity.ok(watchlist);
    }

    @PostMapping
    @Operation(summary = "Add a stock symbol to watchlist")
    public ResponseEntity<WatchlistResponseDTO> addToWatchlist(@Valid @RequestBody WatchlistRequestDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        WatchlistResponseDTO result = watchlistService.addToWatchlist(email, dto);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a stock from watchlist")
    public ResponseEntity<SuccessResponse> removeFromWatchlist(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        watchlistService.removeFromWatchlist(email, id);
        return ResponseEntity.ok(new SuccessResponse("Stock deleted form watchlist"));
    }

    @PutMapping("/update-target/{symbol}")
    @Operation(summary = "Update target price menggunakan Request Body")
    public ResponseEntity<WatchlistResponseDTO> updateTargetPrice(
            @PathVariable String symbol,
            @RequestBody WatchlistUpdateDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        WatchlistResponseDTO result = watchlistService.updateTargetPriceBySymbol(email, symbol, dto);
        return ResponseEntity.ok(result);
    }
}