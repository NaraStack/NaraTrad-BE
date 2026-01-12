package com.naratrad.repository;

import com.naratrad.entity.Portfolio;
import com.naratrad.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findBySymbol(String symbol);
    List<Portfolio> findByUser(User user);
    Optional<Portfolio> findByUserAndSymbol(User user, String symbol);
    Optional<Portfolio> findByUserAndId(User user, Long id);
    @Query("SELECT SUM(p.quantity) FROM Portfolio p")
    Long countTotalSharesGlobally();
    @Query("SELECT COUNT(DISTINCT p.symbol) FROM Portfolio p")
    long countUniqueStocks();
    @Query("SELECT p.symbol, COUNT(p.symbol) as total FROM Portfolio p GROUP BY p.symbol ORDER BY total DESC")
    List<Object[]> findMostPopularStocks(Pageable pageable);
    @Query("SELECT AVG(sub.cnt) FROM (SELECT COUNT(p.id) as cnt FROM Portfolio p GROUP BY p.user.id) sub")
    Double getAveragePortfolioSize();
    @Transactional
    void deleteByUser(User user);
}