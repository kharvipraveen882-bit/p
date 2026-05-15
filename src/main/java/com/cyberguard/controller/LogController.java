package com.cyberguard.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
public class LogController {

    private static String getDbUrl() {
        String host = System.getenv("DB_HOST");
        String port = System.getenv("DB_PORT");
        String name = System.getenv("DB_NAME");
        if (host == null || host.isBlank()) {
            return "jdbc:postgresql://localhost:5432/cyberguard"; 
        }
        return "jdbc:postgresql://" + host + ":" + port + "/" + name;
    }

    private static String getDbUser() {
        return System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "postgres";
    }

    private static String getDbPassword() {
        return System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "postgres";
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPassword());
    }

    @PostConstruct
    public void init() {
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS access_logs (" +
                    "  id SERIAL PRIMARY KEY," +
                    "  userId TEXT NOT NULL," +
                    "  resource TEXT NOT NULL," +
                    "  accessType TEXT NOT NULL," +
                    "  timestamp TEXT NOT NULL," +
                    "  isSuspicious INTEGER DEFAULT 0," +
                    "  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")"
                );
            }
            System.out.println("[CyberGuard] PostgreSQL initialized successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Removed duplicated /DataAccessServlet mapping to fix Ambiguous mapping error

    @GetMapping("/api/logs")
    public List<Map<String, Object>> getRecentLogs() {
        List<Map<String, Object>> logs = new ArrayList<>();
        try (Connection conn = getConnection();
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
        try (Connection conn = getConnection();
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
