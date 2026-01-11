package com.naratrad.dto;

import lombok.Data;
import java.time.LocalDateTime;

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

    // Purchase Information
    private Double purchasePrice;   // Harga beli per saham
    private LocalDateTime createdAt; // Tanggal pembelian

    // Calculated Fields untuk Dashboard FE
    private Double totalInvestment;  // purchasePrice * quantity
    private Double gainLoss;         // (currentPrice - purchasePrice) * quantity
    private Double gainLossPercent;  // ((currentPrice - purchasePrice) / purchasePrice) * 100
}
