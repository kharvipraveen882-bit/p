package com.cyberguard.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
public class LogController {

    private static String getDbUrl() {
        String dbPath = System.getenv("DB_PATH");
        if (dbPath == null || dbPath.isEmpty()) {
            dbPath = "data_access_logs.db";
        }
        return "jdbc:sqlite:" + dbPath;
    }

    @PostConstruct
    public void init() {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(getDbUrl());
                 Statement stmt = conn.createStatement()) {
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS access_logs (" +
                    "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "  userId TEXT NOT NULL," +
                    "  resource TEXT NOT NULL," +
                    "  accessType TEXT NOT NULL," +
                    "  timestamp TEXT NOT NULL," +
                    "  isSuspicious INTEGER DEFAULT 0," +
                    "  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")"
                );
            }
            System.out.println("[CyberGuard] Database initialized successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Removed duplicated /DataAccessServlet mapping to fix Ambiguous mapping error

    @GetMapping("/api/logs")
    public List<Map<String, Object>> getRecentLogs() {
        List<Map<String, Object>> logs = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(getDbUrl());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM access_logs ORDER BY createdAt DESC LIMIT 20")) {
            
            while (rs.next()) {
                Map<String, Object> log = new HashMap<>();
                log.put("id", rs.getInt("id"));
                log.put("userId", rs.getString("userId"));
                log.put("resource", rs.getString("resource"));
                log.put("accessType", rs.getString("accessType"));
                log.put("timestamp", rs.getString("timestamp"));
                log.put("isSuspicious", rs.getInt("isSuspicious") == 1);
                log.put("createdAt", rs.getString("createdAt"));
                logs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    @GetMapping("/api/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(getDbUrl());
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total, SUM(isSuspicious) as suspicious FROM access_logs");
            if (rs.next()) {
                int total = rs.getInt("total");
                int suspicious = rs.getInt("suspicious");
                stats.put("total", total);
                stats.put("suspicious", suspicious);
                stats.put("safe", total - suspicious);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }


}
