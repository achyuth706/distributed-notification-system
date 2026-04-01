package com.nds.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nds.notification.entity.DeadLetterEvent;
import com.nds.notification.repository.DeadLetterRepository;
import com.nds.shared.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterConsumer {

    private final DeadLetterRepository deadLetterRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "notifications.dead-letter",
        groupId = "dead-letter-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeDeadLetter(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.error("╔══════════════════════════════════════════════════════╗");
        log.error("║              DEAD LETTER EVENT RECEIVED               ║");
        log.error("╠══════════════════════════════════════════════════════╣");
        log.error("║ ID:        {}  ║", event.getId());
        log.error("║ User:      {}  ║", event.getUserId());
        log.error("║ Channel:   {}  ║", event.getChannel());
        log.error("║ Priority:  {}  ║", event.getPriority());
        log.error("║ Retries:   {}  ║", event.getRetryCount());
        log.error("║ Error:     {}  ║", event.getErrorMessage());
        log.error("║ Topic:     {} | Partition: {} | Offset: {}  ║", topic, partition, offset);
        log.error("╚══════════════════════════════════════════════════════╝");

        try {
            String payload = objectMapper.writeValueAsString(event);

            DeadLetterEvent deadLetter = DeadLetterEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .notificationId(event.getId())
                    .topic(topic)
                    .partitionId(partition)
                    .offsetValue(offset)
                    .payload(payload)
                    .errorMessage(event.getErrorMessage())
                    .retryCount(event.getRetryCount())
                    .build();

            deadLetterRepository.save(deadLetter);
            log.info("Stored dead letter event for notification {}", event.getId());

        } catch (Exception e) {
            log.error("Failed to persist dead letter event {}: {}", event.getId(), e.getMessage());
        }

        ack.acknowledge();
    }
}
