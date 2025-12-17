package com.naratrad.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "latest_price",
    indexes = {
        @Index(name = "idx_latest_symbol", columnList = "symbol", unique = true)
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LatestPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 16, unique = true)
    private String symbol;

    @Column(precision = 18, scale = 4)
    private BigDecimal current;

    @Column(precision = 18, scale = 4)
    private BigDecimal prevClose;

    @Column(precision = 7, scale = 2)
    private BigDecimal changePercent;

    private OffsetDateTime updatedAt;
}
