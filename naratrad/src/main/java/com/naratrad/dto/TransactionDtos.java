package com.naratrad.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TransactionDtos {

    public record BuySellRequest(
            @NotNull Long portfolioId,
            @NotBlank String symbol,
            @Positive BigDecimal quantity,
            @Positive BigDecimal price,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mmXXX") OffsetDateTime ts
    ) {}

    public record TransactionResponse(
            Long transactionId,
            Long portfolioId,
            String symbol,
            String side,
            BigDecimal quantity,
            BigDecimal price,
            BigDecimal realizedPl,   // null untuk BUY
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mmXXX") OffsetDateTime ts
    ) {}
}
