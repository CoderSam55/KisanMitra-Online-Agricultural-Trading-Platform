package com.kisanmitra;
import org.springframework.web.bind.annotation.*;
import java.time.Instant; import java.util.Map;
@RestController @RequestMapping("/api")
public class HealthController { @GetMapping("/health") public Map<String,Object> health(){ return Map.of("success",true,"message","KisanMitra API is running 🌾","database","connected","databaseSource","postgresql","timestamp", Instant.now().toString()); } }
