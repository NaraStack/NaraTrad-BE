package com.naratrad.service;

import com.naratrad.dto.DashboardDtos.*;
import com.naratrad.repository.HoldingRepository;
import com.naratrad.repository.LatestPriceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    private final HoldingRepository holdingRepo;
    private final LatestPriceRepository priceRepo;

    public DashboardService(HoldingRepository holdingRepo, LatestPriceRepository priceRepo) {
        this.holdingRepo = holdingRepo;
        this.priceRepo = priceRepo;
    }

    public DashboardResponse buildDashboard(Long portfolioId) {

        // 1) Ambil holdings milik portfolio
        var holdings = holdingRepo.findByPortfolioId(portfolioId);

        // 2) Ambil symbol list
        var symbols = holdings.stream()
                .map(h -> h.getSymbol().toUpperCase())
                .toList();

        // 3) Ambil latest prices dari DB (cache Finnhub)
        var prices = priceRepo.findBySymbolIn(symbols);

        // map symbol -> price lookup
        var priceMap = prices.stream()
                .collect(java.util.stream.Collectors.toMap(
                        p -> p.getSymbol().toUpperCase(),
                        p -> p
                ));

        // 4) Build dashboard items
        List<DashboardItem> items = holdings.stream()
                .map(h -> {
                    var lp = priceMap.get(h.getSymbol().toUpperCase());

                    var price = lp != null ? lp.getCurrent() : BigDecimal.ZERO;
                    var change = lp != null ? lp.getChangePercent() : BigDecimal.ZERO;

                    return new DashboardItem(
                            h.getPortfolio().getId().intValue(),
                            h.getSymbol(),
                            price,
                            change,
                            h.getQuantity()
                    );
                })
                .toList();

        // 5) Hitung nilai total portfolio
        var total = items.stream()
                .map(i -> i.price().multiply(i.quantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardResponse(total, items);
    }
}
