package com.nds.email.controller;

import com.nds.email.service.EmailSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmailMetricsController {

    private final EmailSenderService emailSenderService;

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(Map.of(
            "service", "email-service",
            "totalSent", emailSenderService.getTotalSent(),
            "totalFailed", emailSenderService.getTotalFailed(),
            "avgLatencyMs", emailSenderService.getAvgLatencyMs()
        ));
    }
}
