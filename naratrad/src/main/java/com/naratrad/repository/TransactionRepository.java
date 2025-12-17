package com.naratrad.repository;

import com.naratrad.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByPortfolioIdOrderByTsDesc(Long portfolioId);
    List<Transaction> findTop20ByPortfolioIdOrderByTsDesc(Long portfolioId); // untuk riwayat cepat
}
