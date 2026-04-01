package com.nds.notification.consumer;

import com.nds.notification.entity.NotificationEntity;
import com.nds.notification.metrics.NotificationMetrics;
import com.nds.notification.repository.NotificationRepository;
import com.nds.notification.service.TemplateService;
import com.nds.shared.enums.NotificationStatus;
import com.nds.shared.event.NotificationEvent;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private static final int MAX_RETRIES = 3;
    private static final String DEAD_LETTER_TOPIC = "notifications.dead-letter";

    private final NotificationRepository repository;
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final NotificationMetrics metrics;
    private final TemplateService templateService;

    @KafkaListener(
        topics = "notifications.email",
        groupId = "notification-core-group",
        concurrency = "3",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeEmail(@Payload NotificationEvent event,
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                              Acknowledgment ack) {
        processEvent(event, topic, ack);
    }

    @KafkaListener(
        topics = "notifications.sms",
        groupId = "notification-core-group",
        concurrency = "3",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeSms(@Payload NotificationEvent event,
                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                            Acknowledgment ack) {
        processEvent(event, topic, ack);
    }

    @KafkaListener(
        topics = "notifications.push",
        groupId = "notification-core-group",
        concurrency = "3",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePush(@Payload NotificationEvent event,
                             @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                             Acknowledgment ack) {
        processEvent(event, topic, ack);
    }

    private void processEvent(NotificationEvent event, String topic, Acknowledgment ack) {
        metrics.incrementQueueDepth();
        Timer.Sample timerSample = metrics.startTimer();
        long startMs = System.currentTimeMillis();

        log.info("Processing notification {} from topic {} for user {}", event.getId(), topic, event.getUserId());

        try {
            // Apply template if configured
            if (event.getTemplateId() != null) {
                String resolvedBody = templateService.resolveTemplate(event.getTemplateId(), event.getVariables());
                event.setBody(resolvedBody);
            }

            // Simulate processing with exponential backoff retries
            processWithRetry(event, topic, 0);

            event.setStatus(NotificationStatus.SENT);
            event.setProcessedAt(LocalDateTime.now());
            event.setProcessingTimeMs(System.currentTimeMillis() - startMs);

            updateNotificationStatus(event, NotificationStatus.SENT, null);
            metrics.incrementSent(event.getChannel().name(), "SUCCESS");
            metrics.recordDuration(timerSample, event.getChannel().name());

            log.info("Successfully processed notification {} in {}ms", event.getId(), event.getProcessingTimeMs());
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process notification {} after retries: {}", event.getId(), e.getMessage());
            sendToDeadLetter(event, topic, e.getMessage());
            updateNotificationStatus(event, NotificationStatus.DEAD_LETTERED, e.getMessage());
            metrics.incrementSent(event.getChannel().name(), "DEAD_LETTERED");
            metrics.recordDuration(timerSample, event.getChannel().name());
            ack.acknowledge();
        } finally {
            metrics.decrementQueueDepth();
        }
    }

    private void processWithRetry(NotificationEvent event, String topic, int attempt) throws Exception {
        try {
            // Simulate downstream call (will be overridden by channel services)
            log.debug("Processing attempt {} for notification {}", attempt + 1, event.getId());
            // Intentionally no-op here; channel workers handle actual delivery
        } catch (Exception e) {
            if (attempt >= MAX_RETRIES - 1) {
                throw e;
            }
            long backoffMs = (long) Math.pow(2, attempt) * 1000;
            log.warn("Retry {}/{} for notification {} after {}ms: {}", attempt + 1, MAX_RETRIES, event.getId(), backoffMs, e.getMessage());
            try {
                Thread.sleep(backoffMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw e;
            }
            processWithRetry(event, topic, attempt + 1);
        }
    }

    private void sendToDeadLetter(NotificationEvent event, String topic, String errorMessage) {
        event.setStatus(NotificationStatus.DEAD_LETTERED);
        event.setErrorMessage(errorMessage);
        kafkaTemplate.send(DEAD_LETTER_TOPIC, event.getId(), event);
        log.warn("Sent notification {} to dead-letter topic", event.getId());
    }

    private void updateNotificationStatus(NotificationEvent event, NotificationStatus status, String errorMessage) {
        try {
            repository.findById(event.getId()).ifPresent(entity -> {
                entity.setStatus(status);
                entity.setErrorMessage(errorMessage);
                if (status == NotificationStatus.SENT) {
                    entity.setSentAt(LocalDateTime.now());
                }
                repository.save(entity);
            });
        } catch (Exception e) {
            log.error("Failed to update notification status for {}: {}", event.getId(), e.getMessage());
        }
    }
}
