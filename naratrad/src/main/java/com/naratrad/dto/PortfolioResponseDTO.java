package com.naratrad.dto;

import lombok.Data;

@Data
public class PortfolioResponseDTO {
    private Long id;
    private String symbol;
    private Integer quantity;

    // Data dari Finnhub API
    private Double price;       // Current price (c)
    private Double change;      // Percent change (dp)
    private Double totalValue;  // quantity * price

    // Field Baru
    private Double previousClose; // Harga penutupan kemarin (pc)
    private Double priceChange;   // Selisih harga dalam USD (d)
}

