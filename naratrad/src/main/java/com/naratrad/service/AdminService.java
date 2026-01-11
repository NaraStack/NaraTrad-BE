package com.naratrad.service;

import com.naratrad.dto.AdminDashboardDTO;
import com.naratrad.entity.Portfolio;
import com.naratrad.entity.User;
import com.naratrad.repository.ApiLogRepository;
import com.naratrad.repository.PortfolioRepository;
import com.naratrad.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class AdminService {
    @Autowired private UserRepository userRepository;
    @Autowired private PortfolioRepository portfolioRepository;
    @Autowired private ApiLogRepository apiLogRepository; // Repositori baru untuk log

    private List<Map<String, Object>> getUserGrowth() {
        List<User> allUsers = userRepository.findAll(); // Pastikan ambil SEMUA

        // Kelompokkan berdasarkan tanggal created_at
        Map<LocalDate, Long> growthMap = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedAt().toLocalDate(),
                        TreeMap::new, // Pakai TreeMap supaya urut berdasarkan tanggal
                        Collectors.counting()
                ));

        return growthMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("date", entry.getKey().toString());
                    data.put("count", entry.getValue());
                    return data;
                })
                .collect(Collectors.toList());
    }

    public AdminDashboardDTO getDetailedStats() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

        // --- Logic User Management ---
        long totalUsers = userRepository.count();
        long activeToday = userRepository.countByLastLoginAfter(oneDayAgo);
        long newThisWeek = userRepository.countByCreatedAtAfter(oneWeekAgo);

        // --- Logic Portfolio Stats ---
        long uniqueStocks = portfolioRepository.countUniqueStocks();
        Double avgSize = portfolioRepository.getAveragePortfolioSize();

        // Ambil Top 3 Popular Stocks
        List<Object[]> popularData = portfolioRepository.findMostPopularStocks(PageRequest.of(0, 3));
        List<Map<String, Object>> popularStocksList = popularData.stream()
                .map(obj -> Map.of("symbol", obj[0], "count", obj[1]))
                .collect(Collectors.toList());

        // --- Logic System Metrics (Pura-pura dari DB Log) ---
        long apiCalls = apiLogRepository.count();
        Double avgTime = apiLogRepository.getAverageResponseTime();

        // Ambil semua data portfolio
        List<Portfolio> allPortfolios = portfolioRepository.findAll();

        double totalValue = allPortfolios.stream()
                .mapToDouble(p -> {
                    return p.getPurchasePrice() * p.getQuantity();
                })
                .sum();
        return AdminDashboardDTO.builder()
                .totalUsers(totalUsers)
                .activeUsersToday(activeToday)
                .newUsersThisWeek(newThisWeek)
                .totalUniqueStocks(uniqueStocks)
                .avgPortfolioSize(avgSize != null ? avgSize : 0.0)
                .popularStocks(popularStocksList)
                .totalApiCalls(apiCalls)
                .avgResponseTime(avgTime != null ? avgTime : 0.0)
                .userGrowthData(getUserGrowth())
                .totalMarketValue(totalValue)
                .build();
    }
}