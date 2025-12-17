package com.naratrad.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "txn")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    @Column(nullable = false, length = 16)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private TxnSide side;

    @Column(precision = 24, scale = 8, nullable = false)
    private BigDecimal quantity;

    @Column(precision = 18, scale = 4, nullable = false)
    private BigDecimal price;

    private OffsetDateTime ts;
}
