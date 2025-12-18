package com.naratrad.controller;

import com.naratrad.dto.DashboardDTO;
import com.naratrad.dto.PortfolioResponseDTO;
import com.naratrad.dto.PortfolioSummaryDTO;
import com.naratrad.entity.Portfolio;
import com.naratrad.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
@Tag(name = "Portfolio Management", description = "CRUD untuk dashboard NaraTrad")
@CrossOrigin(origins = "*") // Penting agar bisa diakses oleh Angular nanti
public class PortfolioController {

    @Autowired
    private PortfolioService service;

    @GetMapping
    @Operation(summary = "Mendapatkan semua stok beserta harga real-time")
    public List<PortfolioResponseDTO> getAll() {
        return service.getFullPortfolio();
    }

    @PostMapping
    @Operation(summary = "Menambah stok baru atau update jumlah")
    public Portfolio addStock(@RequestBody Portfolio stock) {
        return service.addOrUpdateStock(stock);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Menghapus stok berdasarkan ID")
    public String deleteStock(@PathVariable Long id) {
        service.deleteStock(id);
        return "Stock deleted successfully";
    }

    @GetMapping("/summary")
    @Operation(summary = "Mendapatkan ringkasan total nilai seluruh portofolio")
    public PortfolioSummaryDTO getSummary() {
        return service.getSummary();
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Mendapatkan data lengkap dashboard (Summary + Stock List)")
    public DashboardDTO getDashboard() {
        return service.getDashboardData();
    }

    @GetMapping("/search")
    @Operation(summary = "Mencari Symbol auto lewat dropdown saat add stock")
    public List<Map<String, String>> search(@RequestParam String query) {
        if (query.length() < 1) return List.of();
        return service.searchSymbols(query);
    }
}