// TransactionService.java (stub)
package com.naratrad.service;

import com.naratrad.dto.TransactionDtos.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;


@Service
public class TransactionService {
    public TransactionResponse buy(BuySellRequest req) {
        return new TransactionResponse(
                1L, req.portfolioId(), req.symbol(), "BUY",
                req.quantity(), req.price(), null,
                req.ts() != null ? req.ts() : LocalDateTime.now()
        );
    }
    public TransactionResponse sell(BuySellRequest req) {
        var realized = req.price().subtract(new BigDecimal("100")).multiply(req.quantity());
        return new TransactionResponse(
                2L, req.portfolioId(), req.symbol(), "SELL",
                req.quantity(), req.price(), realized,
                req.ts() != null ? req.ts() : LocalDateTime.now()
        );
    }
}
