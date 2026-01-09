package com.naratrad.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    // 1. Portfolio Stats
    private long totalUniqueStocks;
    private List<Map<String, Object>> popularStocks;
    private double avgPortfolioSize;
    private double totalMarketValue;

    // 2. System Metrics
    private long totalApiCalls;
    private double avgResponseTime;

    // 3. User Management
    private long totalUsers;
    private long activeUsersToday;
    private long newUsersThisWeek;
    private List<Map<String, Object>> userGrowthData; // Untuk Chart
}