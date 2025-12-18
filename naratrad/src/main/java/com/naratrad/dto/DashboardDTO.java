package com.naratrad.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardDTO {
    private Double totalPortfolioValue;
    private Integer totalStocksOwned;
    private List<PortfolioResponseDTO> stockList; // Ini data detail tiap stock
}