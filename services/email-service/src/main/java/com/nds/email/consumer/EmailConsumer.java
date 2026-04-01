package com.nds.email.consumer;

import com.nds.email.service.EmailSenderService;
import com.nds.shared.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final EmailSenderService emailSenderService;

    @KafkaListener(
        topics = "notifications.email",
        groupId = "email-service-group",
        concurrency = "3",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(@Payload NotificationEvent event,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        Acknowledgment ack) {
        log.info("Email consumer received notification {} for user {}", event.getId(), event.getUserId());
        try {
            emailSenderService.sendEmail(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to send email for notification {}: {}", event.getId(), e.getMessage());
            ack.acknowledge();
        }
    }
}
