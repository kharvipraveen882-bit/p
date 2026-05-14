package com.cyberguard.model;

/**
 * AccessLogRequest - Data model for access log submissions
 */
public class AccessLogRequest {
    private String userId;
    private String resource;
    private String accessType;
    private String timestamp;

    public AccessLogRequest(String userId, String resource, String accessType, String timestamp) {
        this.userId = sanitize(userId);
        this.resource = sanitize(resource);
        this.accessType = sanitize(accessType);
        this.timestamp = sanitize(timestamp);
    }

    /**
     * Sanitize input to prevent XSS attacks
     */
    private static String sanitize(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;")
                   .trim();
    }

    public String getUserId() {
        return userId;
    }

    public String getResource() {
        return resource;
    }

    public String getAccessType() {
        return accessType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isValid() {
        return !userId.isEmpty() && !resource.isEmpty() && !accessType.isEmpty() && !timestamp.isEmpty();
    }
}
