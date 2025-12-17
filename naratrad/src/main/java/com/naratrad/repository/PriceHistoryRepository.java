package com.naratrad.repository;

import com.naratrad.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findBySymbolAndTimestampBetweenOrderByTimestampAsc(
            String symbol, OffsetDateTime start, OffsetDateTime end);
}
