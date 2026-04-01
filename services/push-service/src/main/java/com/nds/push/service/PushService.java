package com.nds.push.service;

import com.nds.shared.event.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class PushService {

    private final ConcurrentHashMap<String, String> pushTokenRegistry = new ConcurrentHashMap<>();
    private final AtomicLong totalSent = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);
    private final AtomicLong totalLatencyMs = new AtomicLong(0);

    public void sendPush(NotificationEvent event) {
        long start = System.currentTimeMillis();

        String pushToken = pushTokenRegistry.computeIfAbsent(
            event.getUserId(),
            uid -> "PUSH_TOKEN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
        );

        String messageId = "projects/nds-mock/messages/" + UUID.randomUUID();

        long elapsed = System.currentTimeMillis() - start;
        totalSent.incrementAndGet();
        totalLatencyMs.addAndGet(elapsed);

        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║           PUSH NOTIFICATION SENT (FIREBASE)           ║");
        log.info("╠══════════════════════════════════════════════════════╣");
        log.info("║ Message ID : {}  ║", messageId);
        log.info("║ ID         : {}  ║", event.getId());
        log.info("║ User       : {}  ║", event.getUserId());
        log.info("║ Token      : {}  ║", pushToken);
        log.info("║ Title      : {}  ║", event.getSubject());
        log.info("║ Priority   : {}  ║", event.getPriority());
        log.info("║ Timestamp  : {}  ║", LocalDateTime.now());
        log.info("║ Latency    : {}ms  ║", elapsed);
        log.info("╚══════════════════════════════════════════════════════╝");
    }

    public Map<String, String> getPushTokenRegistry() { return Map.copyOf(pushTokenRegistry); }
    public long getTotalSent()    { return totalSent.get(); }
    public long getTotalFailed()  { return totalFailed.get(); }
    public long getAvgLatencyMs() {
        long sent = totalSent.get();
        return sent > 0 ? totalLatencyMs.get() / sent : 0;
    }
}
