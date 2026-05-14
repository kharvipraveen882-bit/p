package com.cyberguard.servlet;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * DataAccessServlet
 * -----------------
 * Handles POST requests from the CyberGuard dashboard.
 * - Validates input fields
 * - Detects suspicious access (DELETE or admin resource)
 * - Stores records in SQLite via JDBC / PreparedStatement
 * - Returns a styled HTML response page
 */
public class DataAccessServlet extends HttpServlet {

    // ── DB URL ────────────────────────────────────────────────
    private static String getDbUrl() {
        String path = System.getenv("DB_PATH");
        if (path == null || path.isBlank()) {
            path = System.getProperty("user.home") + "/data_access_logs.db";
        }
        return "jdbc:sqlite:" + path;
    }

    // ── Init: create table if not exists ─────────────────────
    @Override
    public void init() throws ServletException {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection c = DriverManager.getConnection(getDbUrl());
                 Statement  s = c.createStatement()) {
                s.execute(
                    "CREATE TABLE IF NOT EXISTS access_logs (" +
                    "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "  userId      TEXT    NOT NULL," +
                    "  resource    TEXT    NOT NULL," +
                    "  accessType  TEXT    NOT NULL," +
                    "  timestamp   TEXT    NOT NULL," +
                    "  isSuspicious INTEGER DEFAULT 0," +
                    "  createdAt   DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")"
                );
            }
            log("[CyberGuard] SQLite initialized → " + getDbUrl());
        } catch (ClassNotFoundException e) {
            throw new ServletException("sqlite-jdbc.jar not found in WEB-INF/lib", e);
        } catch (SQLException e) {
            throw new ServletException("Database initialization failed", e);
        }
    }

    // ── GET Handler (for API) ────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) return;

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        try (Connection c = DriverManager.getConnection(getDbUrl())) {
            if ("stats".equals(action)) {
                int total = 0, suspicious = 0;
                try (Statement s = c.createStatement();
                     ResultSet rs = s.executeQuery("SELECT COUNT(*) as t, SUM(isSuspicious) as s FROM access_logs")) {
                    if (rs.next()) {
                        total = rs.getInt("t");
                        suspicious = rs.getInt("s");
                    }
                }
                out.printf("{\"total\":%d,\"suspicious\":%d,\"safe\":%d}", total, suspicious, total - suspicious);
            } else if ("logs".equals(action)) {
                StringBuilder json = new StringBuilder("[");
                try (Statement s = c.createStatement();
                     ResultSet rs = s.executeQuery("SELECT userId, resource, accessType, timestamp, isSuspicious FROM access_logs ORDER BY id DESC LIMIT 50")) {
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) json.append(",");
                        json.append(String.format("{\"userId\":\"%s\",\"resource\":\"%s\",\"accessType\":\"%s\",\"timestamp\":\"%s\",\"isSuspicious\":%b}",
                                rs.getString("userId"), rs.getString("resource"), rs.getString("accessType"),
                                rs.getString("timestamp"), rs.getInt("isSuspicious") == 1));
                        first = false;
                    }
                }
                json.append("]");
                out.print(json.toString());
            }
        } catch (SQLException e) {
            log("[CyberGuard] DB read failed: " + e.getMessage());
            out.print("{}");
        }
    }

    // ── POST Handler ─────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        // Read & sanitize
        String userId     = sanitize(req.getParameter("userId"));
        String resource   = sanitize(req.getParameter("resource"));
        String accessType = sanitize(req.getParameter("accessType"));
        String timestamp  = sanitize(req.getParameter("timestamp"));

        // Validate
        if (userId.isEmpty() || resource.isEmpty() || accessType.isEmpty() || timestamp.isEmpty()) {
            out.println(errorPage("All fields are required. Please go back and fill in every field."));
            return;
        }

        // Detect suspicious access
        boolean suspicious =
            "DELETE".equalsIgnoreCase(accessType) ||
            "admin".equalsIgnoreCase(resource);

        // Insert into SQLite
        boolean saved = false;
        try (Connection c = DriverManager.getConnection(getDbUrl());
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO access_logs (userId, resource, accessType, timestamp, isSuspicious) VALUES (?,?,?,?,?)"
             )) {
            ps.setString(1, userId);
            ps.setString(2, resource);
            ps.setString(3, accessType);
            ps.setString(4, timestamp);
            ps.setInt(5, suspicious ? 1 : 0);
            ps.executeUpdate();
            saved = true;
        } catch (SQLException e) {
            log("[CyberGuard] DB insert failed: " + e.getMessage());
        }

        out.println(responsePage(userId, resource, accessType, timestamp, suspicious, saved));
    }

    // ── Sanitize: prevent XSS ────────────────────────────────
    private String sanitize(String in) {
        if (in == null) return "";
        return in.replace("&","&amp;")
                 .replace("<","&lt;")
                 .replace(">","&gt;")
                 .replace("\"","&quot;")
                 .replace("'","&#x27;")
                 .trim();
    }

    // ── Response Page ─────────────────────────────────────────
    private String responsePage(String userId, String resource,
                                String accessType, String timestamp,
                                boolean suspicious, boolean saved) {

        String color   = suspicious ? "#ff3366" : "#00ff88";
        String icon    = suspicious ? "&#x26A0;" : "&#x2713;";
        String title   = suspicious ? "SUSPICIOUS ACCESS DETECTED" : "ACCESS LOGGED SUCCESSFULLY";
        String sub     = suspicious ? "This event has been flagged for security review"
                                    : "Event recorded and verified as safe";
        String bgGrad  = suspicious ? "linear-gradient(135deg,#160820,#0c1226)"
                                    : "linear-gradient(135deg,#061422,#062212)";
        String dbMsg   = saved ? "&#x2713; Record saved to SQLite database"
                               : "&#x2717; Database unavailable — record not persisted";
        String dbColor = saved ? "#00ff88" : "#ff3366";

        return "<!DOCTYPE html><html lang='en'><head>" +
            "<meta charset='UTF-8'/>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'/>" +
            "<title>CyberGuard – Analysis Result</title>" +
            "<link href='https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700&family=Rajdhani:wght@400;600&family=Share+Tech+Mono&display=swap' rel='stylesheet'/>" +
            "<style>" +
            "*{margin:0;padding:0;box-sizing:border-box}" +
            "body{font-family:'Rajdhani',sans-serif;background:" + bgGrad + ";color:#e0eaff;" +
              "min-height:100vh;display:flex;align-items:center;justify-content:center;padding:2rem;" +
              "background-attachment:fixed;}" +
            "body::before{content:'';position:fixed;inset:0;" +
              "background:repeating-linear-gradient(0deg,transparent,transparent 2px,rgba(0,200,255,.008) 2px,rgba(0,200,255,.008) 4px);" +
              "pointer-events:none;}" +
            ".card{background:rgba(12,20,44,.80);backdrop-filter:blur(24px);" +
              "border:1px solid rgba(0,200,255,.12);border-radius:18px;" +
              "padding:2.5rem;max-width:540px;width:100%;position:relative;overflow:hidden;" +
              "box-shadow:0 20px 60px rgba(0,0,0,.5);}" +
            ".card::before{content:'';position:absolute;top:0;left:0;right:0;height:3px;" +
              "background:linear-gradient(90deg,transparent," + color + ",transparent);}" +
            ".icon{font-size:2.8rem;color:" + color + ";text-align:center;margin-bottom:.8rem;" +
              "animation:pop .5s cubic-bezier(.4,0,.2,1);}" +
            "@keyframes pop{0%{transform:scale(.6);opacity:0}100%{transform:scale(1);opacity:1}}" +
            "h1{font-family:'Orbitron',sans-serif;font-size:1rem;text-align:center;" +
              "color:" + color + ";letter-spacing:3px;margin-bottom:.25rem;}" +
            ".sub{text-align:center;font-size:.82rem;color:#7a8bb5;margin-bottom:1.6rem;}" +
            ".grid{display:grid;grid-template-columns:1fr 1fr;gap:.65rem;margin-bottom:1rem;}" +
            ".cell{padding:.7rem .9rem;background:rgba(0,0,0,.3);border-radius:8px;border-left:3px solid #00c8ff;}" +
            ".lbl{font-size:.58rem;color:#3a4568;text-transform:uppercase;letter-spacing:1px;}" +
            ".val{font-family:'Share Tech Mono',monospace;font-size:.88rem;color:#fff;margin-top:.12rem;}" +
            ".db{text-align:center;font-size:.75rem;color:" + dbColor + ";" +
              "background:rgba(0,0,0,.25);padding:.55rem;border-radius:8px;margin-top:.5rem;}" +
            ".back{display:block;text-align:center;margin-top:1.6rem;padding:.9rem;" +
              "background:linear-gradient(135deg,#00c8ff,#0070cc);color:#fff;" +
              "font-family:'Orbitron',sans-serif;font-size:.75rem;letter-spacing:2px;" +
              "border-radius:9px;text-decoration:none;transition:.3s;}" +
            ".back:hover{box-shadow:0 6px 24px rgba(0,200,255,.4);transform:translateY(-2px);}" +
            "</style></head><body>" +
            "<div class='card'>" +
              "<div class='icon'>" + icon + "</div>" +
              "<h1>" + title + "</h1>" +
              "<p class='sub'>" + sub + "</p>" +
              "<div class='grid'>" +
                "<div class='cell'><div class='lbl'>User ID</div><div class='val'>" + userId + "</div></div>" +
                "<div class='cell'><div class='lbl'>Resource</div><div class='val'>" + resource + "</div></div>" +
                "<div class='cell'><div class='lbl'>Access Type</div><div class='val'>" + accessType + "</div></div>" +
                "<div class='cell'><div class='lbl'>Timestamp</div><div class='val'>" + timestamp + "</div></div>" +
              "</div>" +
              "<div class='db'>" + dbMsg + "</div>" +
              "<a href='index.html' class='back'>&#x2190; BACK TO DASHBOARD</a>" +
            "</div></body></html>";
    }

    // ── Error Page ───────────────────────────────────────────
    private String errorPage(String msg) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'/><title>Error – CyberGuard</title>" +
            "<link href='https://fonts.googleapis.com/css2?family=Orbitron:wght@700&family=Rajdhani:wght@400;600&display=swap' rel='stylesheet'/>" +
            "<style>" +
            "*{margin:0;padding:0;box-sizing:border-box}" +
            "body{font-family:'Rajdhani',sans-serif;background:linear-gradient(135deg,#060a14,#160820);" +
              "color:#ff3366;display:flex;align-items:center;justify-content:center;min-height:100vh;}" +
            ".box{background:rgba(12,20,44,.8);border:1px solid rgba(255,51,102,.2);border-radius:14px;" +
              "padding:2.5rem;max-width:400px;width:90%;text-align:center;}" +
            "h2{font-family:'Orbitron',sans-serif;font-size:1rem;letter-spacing:2px;margin-bottom:1rem;}" +
            "p{font-size:.9rem;color:#b0b8cc;margin-bottom:1.5rem;}" +
            "a{display:inline-block;color:#00c8ff;font-size:.82rem;text-decoration:none;" +
              "border:1px solid rgba(0,200,255,.2);padding:.6rem 1.4rem;border-radius:8px;transition:.3s;}" +
            "a:hover{background:rgba(0,200,255,.08);}" +
            "</style></head>" +
            "<body><div class='box'>" +
            "<h2>&#x26A0; Input Error</h2>" +
            "<p>" + msg + "</p>" +
            "<a href='index.html'>&#x2190; Back to Dashboard</a>" +
            "</div></body></html>";
    }
}
