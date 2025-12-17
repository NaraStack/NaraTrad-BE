package com.naratrad.controller;

import com.naratrad.entity.Portfolio;
import com.naratrad.repository.PortfolioRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/portfolios")
public class PortfolioController {

    private final PortfolioRepository repo;

    public PortfolioController(PortfolioRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public ResponseEntity<Portfolio> create(@Valid @RequestBody Portfolio body) {
        var saved = repo.save(body);
        return ResponseEntity.created(URI.create("/api/v1/portfolios/" + saved.getId())).body(saved);
    }

    @GetMapping
    public List<Portfolio> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Portfolio> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
