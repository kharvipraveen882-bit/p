package com.cyberguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * CyberGuard - Data Access Log Analyzer
 * Spring Boot Application Main Class
 * 
 * A cybersecurity monitoring dashboard for analyzing and detecting suspicious data access patterns.
 * Runs on embedded Tomcat with PostgreSQL database backend, deployable to Render.
 */
@ServletComponentScan
@SpringBootApplication
public class DataAccessLogAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataAccessLogAnalyzerApplication.class, args);
    }
}
