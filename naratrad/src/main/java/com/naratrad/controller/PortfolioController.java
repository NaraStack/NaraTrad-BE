package com.naratrad.controller;

import com.naratrad.dto.*;
import com.naratrad.entity.Portfolio;
import com.naratrad.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @PutMapping("/update-stock/{symbol}")
    @Operation(summary = "Update stok berdasarkan symbol")
    public ResponseEntity<?> updateStock(
            @PathVariable String symbol,
            @RequestBody UpdateStockRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Portfolio stockData = new Portfolio();
        stockData.setSymbol(symbol.toUpperCase());
        stockData.setQuantity(request.getAddedQuantity());

        stockData.setPurchasePrice(0.0);

        try {
            Portfolio updated = service.addOrUpdateStock(email, stockData);

            return ResponseEntity.ok(Map.of(
                    "message", "Stok " + symbol + " berhasil ditambah dengan harga pasar terbaru!",
                    "newQuantity", updated.getQuantity(),
                    "newAveragePrice", updated.getPurchasePrice()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

    @GetMapping("/price/{symbol}")
    @Operation(summary = "Get real-time prices for just one symbol")
    public Double getStockPrice(@PathVariable String symbol) {
        return service.getLivePrice(symbol);
    }

    @GetMapping("/calculate")
    @Operation(summary = "Calculate total value (Price x Quantity) in real-time")
    public Map<String, Object> getCalculation(
            @RequestParam String symbol,
            @RequestParam Integer quantity) {
        return service.calculateTotalValue(symbol, quantity);
    }

    @GetMapping("/export")
    @Operation(summary = "Download portofolio dalam format CSV")
    public ResponseEntity<Resource> downloadCsv() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        InputStreamResource file = new InputStreamResource(service.exportPortfolioToCsv(email));
        String filename = "NaraTrad_Portfolio_" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(file);
    }
}