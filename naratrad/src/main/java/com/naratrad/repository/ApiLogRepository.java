package com.naratrad.repository;

import com.naratrad.entity.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiLogRepository extends JpaRepository<ApiLog, Long> {

    // Menghitung rata-rata waktu respon dari seluruh log API
    @Query("SELECT AVG(a.responseTimeMs) FROM ApiLog a")
    Double getAverageResponseTime();

    // Untuk total calls, kita cukup gunakan method bawaan .count()
}