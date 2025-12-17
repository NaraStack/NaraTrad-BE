package com.naratrad.repository;

import com.naratrad.entity.LatestPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LatestPriceRepository extends JpaRepository<LatestPrice, Long> {
    Optional<LatestPrice> findBySymbolIgnoreCase(String symbol);
    List<LatestPrice> findBySymbolIn(Collection<String> symbols);
}
