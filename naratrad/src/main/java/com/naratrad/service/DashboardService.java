// DashboardService.java
package com.naratrad.service;

import com.naratrad.dto.DashboardDtos.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {
    public DashboardResponse buildDashboard(Long portfolioId) {
        var items = List.of(
                new DashboardItem("AAPL", new BigDecimal("176.50"), new BigDecimal("0.68"), new BigDecimal("25")),
                new DashboardItem("GOOGL", new BigDecimal("130.75"), new BigDecimal("0.21"), new BigDecimal("40")),
                new DashboardItem("TSLA", new BigDecimal("412.00"), new BigDecimal("-0.90"), new BigDecimal("10")),
                new DashboardItem("AMZN", new BigDecimal("3550.46"), new BigDecimal("0.12"), new BigDecimal("5"))
        );
        var portfolioValue = items.stream()
                .map(i -> i.price().multiply(i.quantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardResponse(portfolioValue, items);
    }
}
