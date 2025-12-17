package com.naratrad.controller;

import com.naratrad.dto.TransactionDtos.BuySellRequest;
import com.naratrad.dto.TransactionDtos.TransactionResponse;
import com.naratrad.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService txnService;

    public TransactionController(TransactionService txnService) {
        this.txnService = txnService;
    }

    @PostMapping("/buy")
    public ResponseEntity<TransactionResponse> buy(@Valid @RequestBody BuySellRequest req) {
        return ResponseEntity.ok(txnService.buy(req));
    }

    @PostMapping("/sell")
    public ResponseEntity<TransactionResponse> sell(@Valid @RequestBody BuySellRequest req) {
        return ResponseEntity.ok(txnService.sell(req));
    }
}
