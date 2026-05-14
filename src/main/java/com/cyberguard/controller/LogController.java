package com.cyberguard.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.Map;

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

    @PostMapping(value = "/DataAccessServlet", produces = MediaType.TEXT_HTML_VALUE)
    public String handleLogRequest(
            @RequestParam String userId,
            @RequestParam String resource,
            @RequestParam String accessType,
            @RequestParam String timestamp) {

        // Sanitize
        userId = sanitize(userId);
        resource = sanitize(resource);
        accessType = sanitize(accessType);
        timestamp = sanitize(timestamp);

        if (userId.isEmpty() || resource.isEmpty() || accessType.isEmpty() || timestamp.isEmpty()) {
            return buildErrorPage("All fields are required.");
        }

        boolean isSuspicious = "DELETE".equalsIgnoreCase(accessType) || "admin".equalsIgnoreCase(resource);

        boolean dbSuccess = false;
        try (Connection conn = DriverManager.getConnection(getDbUrl());
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO access_logs (userId, resource, accessType, timestamp, isSuspicious) VALUES (?, ?, ?, ?, ?)"
             )) {
            pstmt.setString(1, userId);
            pstmt.setString(2, resource);
            pstmt.setString(3, accessType);
            pstmt.setString(4, timestamp);
            pstmt.setInt(5, isSuspicious ? 1 : 0);
            pstmt.executeUpdate();
            dbSuccess = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return buildResponsePage(userId, resource, accessType, timestamp, isSuspicious, dbSuccess);
    }

    private String sanitize(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                     .replace("\"", "&quot;").replace("'", "&#x27;").trim();
    }

    private String buildResponsePage(String userId, String resource, String accessType,
                                      String timestamp, boolean isSuspicious, boolean dbSuccess) {
        String statusColor = isSuspicious ? "#ff3366" : "#00ff88";
        String statusIcon = isSuspicious ? "&#x26A0;" : "&#x2713;";
        String statusText = isSuspicious ? "SUSPICIOUS ACCESS DETECTED" : "ACCESS LOGGED SUCCESSFULLY";
        String bgGradient = isSuspicious
            ? "linear-gradient(135deg, #1a0a2e, #2d0a0a)"
            : "linear-gradient(135deg, #0a1a2e, #0a2d1a)";

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='en'><head>")
          .append("<meta charset='UTF-8'/><meta name='viewport' content='width=device-width,initial-scale=1.0'/>")
          .append("<title>CyberGuard - Analysis Result</title>")
          .append("<link href='https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700&family=Rajdhani:wght@400;600&family=Share+Tech+Mono&display=swap' rel='stylesheet'/>")
          .append("<style>")
          .append("*{margin:0;padding:0;box-sizing:border-box}")
          .append("body{font-family:'Rajdhani',sans-serif;background:").append(bgGradient).append(";color:#e0eaff;min-height:100vh;display:flex;align-items:center;justify-content:center;padding:2rem}")
          .append(".card{background:rgba(15,23,50,0.7);backdrop-filter:blur(20px);border:1px solid rgba(0,200,255,0.12);border-radius:18px;padding:2.5rem;max-width:550px;width:100%;position:relative;overflow:hidden}")
          .append(".card::before{content:'';position:absolute;top:0;left:0;right:0;height:3px;background:linear-gradient(90deg,transparent,").append(statusColor).append(",transparent)}")
          .append(".icon{font-size:3rem;color:").append(statusColor).append(";text-align:center;margin-bottom:1rem}")
          .append("h1{font-family:'Orbitron',sans-serif;font-size:1.1rem;text-align:center;color:").append(statusColor).append(";letter-spacing:3px;margin-bottom:0.3rem}")
          .append(".sub{text-align:center;font-size:0.85rem;color:#7a8bb5;margin-bottom:1.5rem}")
          .append(".details{display:grid;grid-template-columns:1fr 1fr;gap:0.6rem}")
          .append(".detail{padding:0.7rem;background:rgba(0,0,0,0.3);border-radius:8px;border-left:3px solid #00c8ff}")
          .append(".detail-label{font-size:0.6rem;color:#4a5578;text-transform:uppercase;letter-spacing:1px}")
          .append(".detail-value{font-family:'Share Tech Mono',monospace;font-size:0.9rem;color:#fff;margin-top:0.15rem}")
          .append(".db-status{text-align:center;margin-top:1.2rem;font-size:0.78rem;color:").append(dbSuccess ? "#00ff88" : "#ff3366").append("}")
          .append(".back-btn{display:block;width:100%;margin-top:1.5rem;padding:0.85rem;border:none;border-radius:8px;background:linear-gradient(135deg,#00c8ff,#0080cc);color:#fff;font-family:'Orbitron',sans-serif;font-size:0.78rem;letter-spacing:2px;cursor:pointer;text-decoration:none;text-align:center;transition:0.3s}")
          .append(".back-btn:hover{box-shadow:0 4px 20px rgba(0,200,255,0.35)}")
          .append("</style></head><body>")
          .append("<div class='card'>")
          .append("<div class='icon'>").append(statusIcon).append("</div>")
          .append("<h1>").append(statusText).append("</h1>")
          .append("<p class='sub'>").append(isSuspicious ? "This event has been flagged for security review" : "Event recorded and verified as safe").append("</p>")
          .append("<div class='details'>")
          .append("<div class='detail'><div class='detail-label'>User ID</div><div class='detail-value'>").append(userId).append("</div></div>")
          .append("<div class='detail'><div class='detail-label'>Resource</div><div class='detail-value'>").append(resource).append("</div></div>")
          .append("<div class='detail'><div class='detail-label'>Access Type</div><div class='detail-value'>").append(accessType).append("</div></div>")
          .append("<div class='detail'><div class='detail-label'>Timestamp</div><div class='detail-value'>").append(timestamp).append("</div></div>")
          .append("</div>")
          .append("<div class='db-status'>").append(dbSuccess ? "&#x2713; Record saved to database" : "&#x2717; Database error — record not saved").append("</div>")
          .append("<a href='/' class='back-btn'>&#x2190; BACK TO DASHBOARD</a>")
          .append("</div></body></html>");
        return sb.toString();
    }

    private String buildErrorPage(String message) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'/><title>Error</title>"
             + "<style>body{font-family:sans-serif;background:#0a0e1a;color:#ff3366;display:flex;align-items:center;justify-content:center;min-height:100vh}"
             + ".box{background:rgba(15,23,50,0.8);border:1px solid rgba(255,51,102,0.2);border-radius:14px;padding:2rem;max-width:400px;text-align:center}"
             + "a{color:#00c8ff;margin-top:1rem;display:inline-block}</style></head>"
             + "<body><div class='box'><h2>Error</h2><p>" + message + "</p><a href='/'>Back to Dashboard</a></div></body></html>";
    }
}
