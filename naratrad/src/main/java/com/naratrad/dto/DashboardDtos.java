package com.naratrad.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDtos {

    public record DashboardItem(
            String symbol,
            BigDecimal price,
            BigDecimal changePercent,
            BigDecimal quantity
    ) {}

    public record DashboardResponse(
            BigDecimal portfolioValue,
            List<DashboardItem> items
    ) {}
}
