package com.kisanmitra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {
    private final DataSource dataSource;
    private final String supabaseUrl;

    public HealthController(DataSource dataSource, @Value("${SUPABASE_URL:}") String supabaseUrl) {
        this.dataSource = dataSource;
        this.supabaseUrl = supabaseUrl == null ? "" : supabaseUrl;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try (var connection = dataSource.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            boolean postgres = product.toLowerCase().contains("postgresql");
            boolean supabase = postgres && supabaseUrl.toLowerCase().contains(".supabase.co");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "KisanMitra API is running 🌾",
                    "database", "connected",
                    "databaseSource", supabase ? "supabase" : product.toLowerCase(),
                    "timestamp", Instant.now().toString()));
        } catch (SQLException error) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "success", false,
                    "message", "KisanMitra API is running, but the database is unavailable.",
                    "database", "disconnected",
                    "timestamp", Instant.now().toString()));
        }
    }
}
