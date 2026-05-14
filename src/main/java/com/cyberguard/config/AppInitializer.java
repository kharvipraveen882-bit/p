package com.cyberguard.config;

import com.cyberguard.service.AccessLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * AppInitializer - Initializes the application on startup
 * Ensures SQLite database is created and ready for use
 */
@Component
public class AppInitializer implements CommandLineRunner {

    @Autowired
    private AccessLogService accessLogService;

    @Override
    public void run(String... args) throws Exception {
        accessLogService.initializeDatabase();
    }
}
