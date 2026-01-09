package com.naratrad.controller;

import com.naratrad.dto.AdminDashboardDTO;
import com.naratrad.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard() {
        // Memanggil method baru yang berisi statistik lengkap (User, Portfolio, & API Metrics)
        AdminDashboardDTO stats = adminService.getDetailedStats();
        return ResponseEntity.ok(stats);
    }
}