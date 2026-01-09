package com.naratrad.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistRequestDTO {
    @NotBlank(message = "Symbol can't be empty")
    private String symbol;

    private Double targetPrice;
}