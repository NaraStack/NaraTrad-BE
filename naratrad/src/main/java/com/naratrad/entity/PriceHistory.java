package com.naratrad.entity;

import jakarta.persistence.*;

@Entity @Table(name="price_history",
        indexes=@Index(name="idx_hist_symbol_ts", columnList="symbol,timestamp"))
public class PriceHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(nullable=false, length=16) String symbol;
    @Column(nullable=false) java.time.OffsetDateTime timestamp;
    @Column(precision=18, scale=4) java.math.BigDecimal price;
}
