package com.naratrad.repository;

import com.naratrad.entity.Portfolio;
import com.naratrad.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    // Find by symbol only (not secure - for backward compatibility)
    Optional<Portfolio> findBySymbol(String symbol);

    // Find all portfolios for certain user
    List<Portfolio> findByUser(User user);

    // Find portfolio by user and symbol
    Optional<Portfolio> findByUserAndSymbol(User user, String symbol);

    // Find portfolio by user and id (for delete operation)
    Optional<Portfolio> findByUserAndId(User user, Long id);

    @Query("SELECT SUM(p.quantity) FROM Portfolio p")
    Long countTotalSharesGlobally();

    // 1. Total unique stocks
    @Query("SELECT COUNT(DISTINCT p.symbol) FROM Portfolio p")
    long countUniqueStocks();

    // 2. Most popular stocks (Top 3)
    @Query("SELECT p.symbol, COUNT(p.symbol) as total FROM Portfolio p GROUP BY p.symbol ORDER BY total DESC")
    List<Object[]> findMostPopularStocks(Pageable pageable);

    // 3. Average portfolio size (rata-rata jumlah baris portfolio per user)
    @Query("SELECT AVG(sub.cnt) FROM (SELECT COUNT(p.id) as cnt FROM Portfolio p GROUP BY p.user.id) sub")
    Double getAveragePortfolioSize();

}