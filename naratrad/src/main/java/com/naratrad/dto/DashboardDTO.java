package com.naratrad.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardDTO {
    // Summary untuk Dashboard
    private Double totalPortfolioValue;   // Total nilai sekarang (current value)
    private Integer totalStocksOwned;     // Jumlah jenis saham berbeda
    private Double totalInvestment;       // Total modal yang diinvestasikan
    private Double totalGainLoss;         // Total keuntungan/kerugian dalam USD
    private Double roi;                   // Return on Investment dalam persen

    // Detail per stock
    private List<PortfolioResponseDTO> stockList; // Data detail tiap stock
}