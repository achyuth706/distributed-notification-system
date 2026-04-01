package com.nds.sms.controller;

import com.nds.sms.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SmsController {

    private final SmsService smsService;

    @GetMapping("/sent")
    public ResponseEntity<List<Map<String, Object>>> getSent() {
        return ResponseEntity.ok(smsService.getSentMessages());
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(Map.of(
            "service", "sms-service",
            "totalSent", smsService.getTotalSent(),
            "totalFailed", smsService.getTotalFailed(),
            "avgLatencyMs", smsService.getAvgLatencyMs()
        ));
    }
}
