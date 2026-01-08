package com.naratrad.controller;

import com.naratrad.dto.DashboardDTO;
import com.naratrad.dto.PerformanceChartDTO;
import com.naratrad.dto.PortfolioResponseDTO;
import com.naratrad.dto.PortfolioSummaryDTO;
import com.naratrad.entity.Portfolio;
import com.naratrad.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
@Tag(name = "Portfolio Management", description = "CRUD untuk dashboard NaraTrad")
public class PortfolioController {

    @Autowired
    private PortfolioService service;

    @GetMapping
    @Operation(summary = "Get all stocks along with real-time prices (filtered by user)")
    public List<PortfolioResponseDTO> getAll() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.getFullPortfolio(email);
    }

    @PostMapping
    @Operation(summary = "Add new stock or update quantity (linked to user)")
    public Portfolio addStock(@RequestBody Portfolio stock) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.addOrUpdateStock(email, stock);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete stock based on ID (verify ownership)")
    public String deleteStock(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        service.deleteStock(email, id);
        return "Stock deleted successfully";
    }

    @GetMapping("/summary")
    @Operation(summary = "Get a summary of the total value of the entire portfolio (filtered by user)")
    public PortfolioSummaryDTO getSummary() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.getSummary(email);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get complete dashboard data (Summary + Stock List, filtered by user)")
    public DashboardDTO getDashboard() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.getDashboardData(email);
    }

    @GetMapping("/performance")
    @Operation(summary = "Get portfolio performance chart data for the last 7 days")
    public PerformanceChartDTO getPerformanceChart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.getPerformanceChart(email);
    }

    @GetMapping("/search")
    @Operation(summary = "Search for auto symbols via dropdown when adding stock")
    public List<Map<String, String>> search(@RequestParam String query) {
        if (query.length() < 1) return List.of();
        return service.searchSymbols(query);
    }

    // 1. Endpoint to get price only
    @GetMapping("/price/{symbol}")
    @Operation(summary = "Get real-time prices for just one symbol")
    public Double getStockPrice(@PathVariable String symbol) {
        return service.getLivePrice(symbol);
    }

    // 2. Endpoint for calculation (usually used in the 'Add Stock' form for simulation)
    @GetMapping("/calculate")
    @Operation(summary = "Calculate total value (Price x Quantity) in real-time")
    public Map<String, Object> getCalculation(
            @RequestParam String symbol,
            @RequestParam Integer quantity) {
        return service.calculateTotalValue(symbol, quantity);
    }
}