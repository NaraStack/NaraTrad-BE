package com.naratrad.controller;

import com.naratrad.service.MarketService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/prices")
public class PriceController {

    private final MarketService market;

    public PriceController(MarketService market) {
        this.market = market;
    }

    // contoh: /api/v1/prices/latest?symbols=AAPL,MSFT,NVDA
    @GetMapping("/latest")
    public Map<String, Object> latest(@RequestParam String symbols) {
        return market.getLatestPrices(symbols);
    }
}
