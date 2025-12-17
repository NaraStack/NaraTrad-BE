package com.naratrad.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "holding",
        indexes = {
                @Index(name = "idx_holding_symbol", columnList = "symbol"),
                @Index(name = "idx_holding_portfolio", columnList = "portfolio_id")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    @Column(nullable = false, length = 16)
    private String symbol;

    @Column(precision = 24, scale = 8, nullable = false)
    private BigDecimal quantity;

    @Column(precision = 18, scale = 4, nullable = false)
    private BigDecimal avgCost;
}
