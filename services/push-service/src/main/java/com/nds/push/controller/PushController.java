package com.nds.push.controller;

import com.nds.push.service.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PushController {

    private final PushService pushService;

    @GetMapping("/tokens")
    public ResponseEntity<Map<String, String>> getPushTokens() {
        return ResponseEntity.ok(pushService.getPushTokenRegistry());
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(Map.of(
            "service", "push-service",
            "totalSent", pushService.getTotalSent(),
            "totalFailed", pushService.getTotalFailed(),
            "avgLatencyMs", pushService.getAvgLatencyMs(),
            "registeredTokens", pushService.getPushTokenRegistry().size()
        ));
    }
}
