package com.naratrad.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "watchlists")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Watchlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String symbol;

    private Double targetPrice;
}