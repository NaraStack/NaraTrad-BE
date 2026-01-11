package com.naratrad.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistResponseDTO {
    private Long id;
    private String symbol;

    // Real-time data from Finnhub
    private Double price;           // Current price
    private Double change;          // Percent change (%)
    private Double priceChange;     // Dollar change
    private Long volume;            // Trading volume

    // User's target price (from entity)
    private Double targetPrice;

    // Metadata
    private String createdAt;
}