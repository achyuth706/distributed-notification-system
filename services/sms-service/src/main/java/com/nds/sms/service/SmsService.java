package com.nds.sms.service;

import com.nds.shared.event.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class SmsService {

    private final CopyOnWriteArrayList<Map<String, Object>> sentMessages = new CopyOnWriteArrayList<>();
    private final AtomicLong totalSent = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);
    private final AtomicLong totalLatencyMs = new AtomicLong(0);

    public void sendSms(NotificationEvent event) {
        long start = System.currentTimeMillis();
        String fakeSid = "SM" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);

        long elapsed = System.currentTimeMillis() - start;
        totalSent.incrementAndGet();
        totalLatencyMs.addAndGet(elapsed);

        Map<String, Object> record = Map.of(
            "sid", fakeSid,
            "notificationId", event.getId(),
            "userId", event.getUserId(),
            "body", event.getBody() != null ? event.getBody() : "(no body)",
            "status", "sent",
            "timestamp", LocalDateTime.now().toString(),
            "latencyMs", elapsed
        );
        sentMessages.add(0, record);
        if (sentMessages.size() > 100) {
            sentMessages.subList(100, sentMessages.size()).clear();
        }

        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║               SMS SENT SUCCESSFULLY                   ║");
        log.info("╠══════════════════════════════════════════════════════╣");
        log.info("║ SID      : {}  ║", fakeSid);
        log.info("║ ID       : {}  ║", event.getId());
        log.info("║ User     : {}  ║", event.getUserId());
        log.info("║ Body     : {}  ║", event.getBody());
        log.info("║ Priority : {}  ║", event.getPriority());
        log.info("║ Latency  : {}ms  ║", elapsed);
        log.info("╚══════════════════════════════════════════════════════╝");
    }

    public List<Map<String, Object>> getSentMessages() { return new ArrayList<>(sentMessages); }
    public long getTotalSent()    { return totalSent.get(); }
    public long getTotalFailed()  { return totalFailed.get(); }
    public long getAvgLatencyMs() {
        long sent = totalSent.get();
        return sent > 0 ? totalLatencyMs.get() / sent : 0;
    }
}
