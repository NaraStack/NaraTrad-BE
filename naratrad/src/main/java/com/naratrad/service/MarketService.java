// MarketService.java
package com.naratrad.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MarketService {
    public Map<String, Object> getLatestPrices(String symbolsCsv) {
        var map = new LinkedHashMap<String, Object>();
        for (var s : symbolsCsv.split(",")) {
            var symbol = s.trim().toUpperCase();
            map.put(symbol, Map.of(
                    "price", 100 + (int)(Math.random() * 50),
                    "changePercent", Math.round((Math.random() * 2 - 1) * 100.0) / 100.0,
                    "prevClose", 100
            ));
        }
        return map;
    }
}
