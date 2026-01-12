package com.naratrad.repository;

import com.naratrad.entity.User;
import com.naratrad.entity.Watchlist;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    List<Watchlist> findByUser(User user);
    Optional<Watchlist> findByUserAndSymbol(User user, String symbol);
    Optional<Watchlist> findByUserAndId(User user, Long id);
    boolean existsByUserAndSymbol(User user, String symbol);
    @Transactional
    void deleteByUser(User user);
}