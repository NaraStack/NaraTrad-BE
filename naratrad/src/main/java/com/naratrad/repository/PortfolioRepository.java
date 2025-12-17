package com.naratrad.repository;

import com.naratrad.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    boolean existsByNameIgnoreCase(String name);
}
