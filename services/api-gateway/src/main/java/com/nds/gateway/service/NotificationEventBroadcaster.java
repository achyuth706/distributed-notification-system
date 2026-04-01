package com.nds.gateway.service;

import com.nds.shared.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventBroadcaster {

    private static final String STREAM_KEY = "notification:stream";
    private static final long STREAM_TTL_HOURS = 1;
    private static final int MAX_STREAM_SIZE = 50;

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    public void broadcastEvent(NotificationEvent event) {
        try {
            // Build JSON payload manually to avoid extra Jackson dependency in interface
            String payload = buildPayload(event);

            // Push to Redis list for REST stream endpoint
            redisTemplate.opsForList().leftPush(STREAM_KEY, payload);
            redisTemplate.opsForList().trim(STREAM_KEY, 0, MAX_STREAM_SIZE - 1);
            redisTemplate.expire(STREAM_KEY, STREAM_TTL_HOURS, TimeUnit.HOURS);

            // Broadcast via WebSocket
            messagingTemplate.convertAndSend("/topic/notifications", payload);
            log.debug("Broadcast notification event {} to /topic/notifications", event.getId());
        } catch (Exception e) {
            log.error("Failed to broadcast event {}: {}", event.getId(), e.getMessage());
        }
    }

    public List<String> getRecentEvents() {
        List<String> events = redisTemplate.opsForList().range(STREAM_KEY, 0, MAX_STREAM_SIZE - 1);
        return events != null ? events : List.of();
    }

    private String buildPayload(NotificationEvent event) {
        return String.format(
            "{\"id\":\"%s\",\"userId\":\"%s\",\"channel\":\"%s\",\"status\":\"%s\",\"timestamp\":\"%s\",\"processingTimeMs\":%d}",
            event.getId(),
            event.getUserId(),
            event.getChannel(),
            event.getStatus(),
            event.getProcessedAt() != null ? event.getProcessedAt() : LocalDateTime.now(),
            event.getProcessingTimeMs()
        );
    }
}
