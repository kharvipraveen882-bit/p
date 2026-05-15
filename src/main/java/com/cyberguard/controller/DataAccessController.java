package com.cyberguard.controller;

import com.cyberguard.service.AccessLogService;
import com.cyberguard.model.AccessLogRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

/**
 * DataAccessController - Handles data access log submissions
 * Receives POST requests with access log data, detects suspicious activity,
 * stores records in PostgreSQL database, and returns stylized HTML response.
 */
@Controller
public class DataAccessController {

    @Autowired
    private AccessLogService accessLogService;

    /**
     * POST endpoint to log access events
     * Accepts: userId, resource, accessType, timestamp
     * Returns: HTML response with analysis results
     */
    @PostMapping(value = "/DataAccessServlet", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String submitAccessLog(
            @RequestParam(name = "userId", defaultValue = "") String userId,
            @RequestParam(name = "resource", defaultValue = "") String resource,
            @RequestParam(name = "accessType", defaultValue = "") String accessType,
            @RequestParam(name = "timestamp", defaultValue = "") String timestamp) {

        AccessLogRequest request = new AccessLogRequest(userId, resource, accessType, timestamp);
        return accessLogService.processAndAnalyze(request);
    }
}
