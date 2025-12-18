package com.naratrad.dto;

import lombok.Data;

@Data
public class PortfolioSummaryDTO {
    private Double totalPortfolioValue; // Penjumlahan semua (quantity * price)
    private Integer totalStocksOwned;   // Jumlah jenis saham (baris di tabel)
}