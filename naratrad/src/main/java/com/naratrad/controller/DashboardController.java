package com.naratrad.controller;

import com.naratrad.dto.DashboardDtos.DashboardItem;
import com.naratrad.dto.DashboardDtos.DashboardResponse;
import com.naratrad.service.DashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    // response: portfolioValue + items [symbol, price, changePercent, quantity]
    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@RequestParam(name = "portfolioId", required = false) Long portfolioId) {
        return service.buildDashboard(portfolioId);
    }
}
