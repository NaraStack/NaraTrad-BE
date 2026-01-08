package com.naratrad.repository;

import com.naratrad.entity.Portfolio;
import com.naratrad.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

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
}