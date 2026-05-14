package com.cyberguard.service;

import com.cyberguard.model.AccessLogRequest;
import org.springframework.stereotype.Service;
import java.sql.*;

/**
 * AccessLogService - Business logic for access log processing
 * Handles database operations, threat detection, and HTML generation
 */
@Service
public class AccessLogService {

    private static final String DB_URL = getDbUrl();

    /**
     * Get database URL from environment variable or use default
     */
    private static String getDbUrl() {
        String dbPath = System.getenv("DB_PATH");
        if (dbPath == null || dbPath.isEmpty()) {
            dbPath = "data_access_logs.db";
        }
        return "jdbc:sqlite:" + dbPath;
    }

    /**
     * Initialize database and create tables if they don't exist
     */
    public void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_URL);
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
                System.out.println("[CyberGuard] Database initialized successfully at: " + DB_URL);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    /**
     * Process access log request, detect threats, save to database, and return HTML response
     */
    public String processAndAnalyze(AccessLogRequest request) {
        // Validate request
        if (!request.isValid()) {
            return buildErrorPage("All fields are required. Please go back and fill in every field.");
        }

        // Detect suspicious access (DELETE operations or admin resource access)
        boolean isSuspicious = "DELETE".equalsIgnoreCase(request.getAccessType())
                || "admin".equalsIgnoreCase(request.getResource());

        // Save to database
        boolean dbSuccess = saveToDatabase(request, isSuspicious);

        // Return response page
        return buildResponsePage(request, isSuspicious, dbSuccess);
    }

    /**
     * Save access log record to SQLite database
     */
    private boolean saveToDatabase(AccessLogRequest request, boolean isSuspicious) {
        try {
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO access_logs (userId, resource, accessType, timestamp, isSuspicious) VALUES (?, ?, ?, ?, ?)"
                 )) {
                pstmt.setString(1, request.getUserId());
                pstmt.setString(2, request.getResource());
                pstmt.setString(3, request.getAccessType());
                pstmt.setString(4, request.getTimestamp());
                pstmt.setInt(5, isSuspicious ? 1 : 0);
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Failed to save to database: " + e.getMessage());
            return false;
        }
    }

    /**
     * Build professional success/warning response HTML page
     */
    private String buildResponsePage(AccessLogRequest request, boolean isSuspicious, boolean dbSuccess) {
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
          .append("<div class='detail'><div class='detail-label'>User ID</div><div class='detail-value'>").append(request.getUserId()).append("</div></div>")
          .append("<div class='detail'><div class='detail-label'>Resource</div><div class='detail-value'>").append(request.getResource()).append("</div></div>")
          .append("<div class='detail'><div class='detail-label'>Access Type</div><div class='detail-value'>").append(request.getAccessType()).append("</div></div>")
          .append("<div class='detail'><div class='detail-label'>Timestamp</div><div class='detail-value'>").append(request.getTimestamp()).append("</div></div>")
          .append("</div>")
          .append("<div class='db-status'>").append(dbSuccess ? "&#x2713; Record saved to database" : "&#x2717; Database error — record not saved").append("</div>")
          .append("<a href='/' class='back-btn'>&#x2190; BACK TO DASHBOARD</a>")
          .append("</div></body></html>");
        return sb.toString();
    }

    /**
     * Build error page
     */
    private String buildErrorPage(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='en'><head>")
          .append("<meta charset='UTF-8'/><meta name='viewport' content='width=device-width,initial-scale=1.0'/>")
          .append("<title>CyberGuard - Error</title>")
          .append("<link href='https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700&family=Rajdhani:wght@400;600&display=swap' rel='stylesheet'/>")
          .append("<style>")
          .append("*{margin:0;padding:0;box-sizing:border-box}")
          .append("body{font-family:'Rajdhani',sans-serif;background:linear-gradient(135deg,#1a0a2e,#0a0e1a);color:#e0eaff;min-height:100vh;display:flex;align-items:center;justify-content:center;padding:2rem}")
          .append(".card{background:rgba(15,23,50,0.7);backdrop-filter:blur(20px);border:1px solid rgba(255,51,102,0.12);border-radius:18px;padding:2.5rem;max-width:550px;width:100%;text-align:center}")
          .append(".icon{font-size:2.5rem;color:#ff3366;margin-bottom:1rem}")
          .append("h1{font-family:'Orbitron',sans-serif;font-size:1rem;color:#ff3366;letter-spacing:2px;margin-bottom:1rem}")
          .append("p{color:#7a8bb5;margin-bottom:1.5rem}")
          .append(".back-btn{display:inline-block;padding:0.75rem 1.5rem;background:linear-gradient(135deg,#ff3366,#cc1a4d);color:#fff;border:none;border-radius:8px;cursor:pointer;text-decoration:none;transition:0.3s}")
          .append(".back-btn:hover{box-shadow:0 4px 20px rgba(255,51,102,0.35)}")
          .append("</style></head><body>")
          .append("<div class='card'>")
          .append("<div class='icon'>&#x26A0;</div>")
          .append("<h1>VALIDATION ERROR</h1>")
          .append("<p>").append(message).append("</p>")
          .append("<a href='/' class='back-btn'>RETURN TO FORM</a>")
          .append("</div></body></html>");
        return sb.toString();
    }
}
