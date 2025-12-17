package com.naratrad.repository;

import com.naratrad.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findByPortfolioId(Long portfolioId);
    Optional<Holding> findByPortfolioIdAndSymbolIgnoreCase(Long portfolioId, String symbol);
    boolean existsByPortfolioIdAndSymbolIgnoreCase(Long portfolioId, String symbol);
    void deleteByPortfolioIdAndSymbolIgnoreCase(Long portfolioId, String symbol);
}
