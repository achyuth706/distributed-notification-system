package com.nds.gateway.controller;

import com.nds.gateway.dto.NotificationRequest;
import com.nds.gateway.dto.NotificationResponse;
import com.nds.gateway.entity.NotificationEntity;
import com.nds.gateway.repository.NotificationRepository;
import com.nds.gateway.service.NotificationEventBroadcaster;
import com.nds.gateway.service.NotificationProducerService;
import com.nds.shared.enums.NotificationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationProducerService producerService;
    private final NotificationRepository repository;
    private final NotificationEventBroadcaster broadcaster;

    @PostMapping
    @Operation(summary = "Send a notification")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest request) {
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
        }
        if (request.getChannels() == null || request.getChannels().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "at least one channel is required"));
        }

        List<NotificationEntity> entities = producerService.sendNotification(request);

        boolean anyRateLimited = entities.stream()
                .anyMatch(e -> e.getStatus() == NotificationStatus.RATE_LIMITED);

        if (anyRateLimited && entities.stream().allMatch(e -> e.getStatus() == NotificationStatus.RATE_LIMITED)) {
            return ResponseEntity.status(429)
                    .body(Map.of("error", "Rate limit exceeded", "ids", entities.stream().map(NotificationEntity::getId).collect(Collectors.toList())));
        }

        List<NotificationResponse> responses = entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification status")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable String id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(toResponse(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user notification history")
    public ResponseEntity<List<NotificationResponse>> getUserHistory(@PathVariable String userId) {
        List<NotificationResponse> history = repository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get system statistics")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNotifications", repository.count());
        stats.put("pending", repository.countByStatus(NotificationStatus.PENDING));
        stats.put("sent", repository.countByStatus(NotificationStatus.SENT));
        stats.put("failed", repository.countByStatus(NotificationStatus.FAILED));
        stats.put("rateLimited", repository.countByStatus(NotificationStatus.RATE_LIMITED));

        Map<String, Long> byChannel = new HashMap<>();
        repository.countByChannel().forEach(row -> byChannel.put((String) row[0], (Long) row[1]));
        stats.put("byChannel", byChannel);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stream")
    @Operation(summary = "Get last 50 events from stream cache")
    public ResponseEntity<List<String>> getStream() {
        return ResponseEntity.ok(broadcaster.getRecentEvents());
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "api-gateway"));
    }

    private NotificationResponse toResponse(NotificationEntity entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .channel(entity.getChannel())
                .status(entity.getStatus())
                .priority(entity.getPriority())
                .subject(entity.getSubject())
                .createdAt(entity.getCreatedAt())
                .sentAt(entity.getSentAt())
                .correlationId(entity.getCorrelationId())
                .errorMessage(entity.getErrorMessage())
                .build();
    }
}
