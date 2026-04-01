package com.nds.push.consumer;

import com.nds.push.service.PushService;
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
public class PushConsumer {

    private final PushService pushService;

    @KafkaListener(
        topics = "notifications.push",
        groupId = "push-service-group",
        concurrency = "3",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(@Payload NotificationEvent event,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        Acknowledgment ack) {
        log.info("Push consumer received notification {} for user {}", event.getId(), event.getUserId());
        try {
            pushService.sendPush(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to send push for notification {}: {}", event.getId(), e.getMessage());
            ack.acknowledge();
        }
    }
}
