package com.nds.email.service;

import com.nds.shared.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

    private final AtomicLong totalSent = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);
    private final AtomicLong totalLatencyMs = new AtomicLong(0);

    public void sendEmail(NotificationEvent event) {
        long start = System.currentTimeMillis();
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@nds.local");
            message.setTo(event.getUserId() + "@example.com");
            message.setSubject(event.getSubject() != null ? event.getSubject() : "Notification");
            message.setText(event.getBody() != null ? event.getBody() : "(no body)");

            mailSender.send(message);

            long elapsed = System.currentTimeMillis() - start;
            totalSent.incrementAndGet();
            totalLatencyMs.addAndGet(elapsed);

            log.info("╔══════════════════════════════════════════════════════╗");
            log.info("║               EMAIL SENT SUCCESSFULLY                 ║");
            log.info("╠══════════════════════════════════════════════════════╣");
            log.info("║ ID      : {}                        ║", event.getId());
            log.info("║ To      : {}@example.com               ║", event.getUserId());
            log.info("║ Subject : {}              ║", event.getSubject());
            log.info("║ Latency : {}ms                                      ║", elapsed);
            log.info("╚══════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            totalFailed.incrementAndGet();
            log.error("Failed to send email for notification {}: {}", event.getId(), e.getMessage());
            throw e;
        }
    }

    public long getTotalSent()    { return totalSent.get(); }
    public long getTotalFailed()  { return totalFailed.get(); }
    public long getAvgLatencyMs() {
        long sent = totalSent.get();
        return sent > 0 ? totalLatencyMs.get() / sent : 0;
    }
}
