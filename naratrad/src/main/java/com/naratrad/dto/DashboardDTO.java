package com.naratrad.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardDTO {
    // Summary for Dashboard
    private Double totalPortfolioValue;   // Total current portfolio value
    private Integer totalStocksOwned;     // Total number of different stocks
    private Double totalInvestment;       // Total capital invested
    private Double totalGainLoss;         // Total profit/loss in USD (since purchase)
    private Double roi;                   // Return on Investment in percentage
    private Double dailyChange;           // Daily change in USD (today only)
    private Double dailyChangePercent;    // Daily change in percentage (today only)

    // Detail per stock
    private List<PortfolioResponseDTO> stockList; // Detailed data for each stock
}