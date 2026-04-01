package com.nds.gateway.service;

import com.nds.gateway.dto.NotificationRequest;
import com.nds.gateway.entity.NotificationEntity;
import com.nds.gateway.repository.NotificationRepository;
import com.nds.shared.enums.Channel;
import com.nds.shared.enums.NotificationStatus;
import com.nds.shared.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducerService {

    private static final String RATE_LIMIT_PREFIX = "rate:";
    private static final long RATE_LIMIT_MAX = 100;
    private static final long RATE_LIMIT_WINDOW_SECONDS = 3600;

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final NotificationRepository notificationRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public List<NotificationEntity> sendNotification(NotificationRequest request) {
        List<NotificationEntity> results = new ArrayList<>();

        for (String channelStr : request.getChannels()) {
            Channel channel;
            try {
                channel = Channel.valueOf(channelStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown channel: {}", channelStr);
                continue;
            }

            String correlationId = UUID.randomUUID().toString();

            // Rate limiting check
            String rateLimitKey = RATE_LIMIT_PREFIX + request.getUserId();
            Long currentCount = redisTemplate.opsForValue().increment(rateLimitKey);
            if (currentCount == 1) {
                redisTemplate.expire(rateLimitKey, RATE_LIMIT_WINDOW_SECONDS, TimeUnit.SECONDS);
            }

            NotificationEntity entity = NotificationEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(request.getUserId())
                    .channel(channel.name())
                    .templateId(request.getTemplateId())
                    .subject(request.getSubject())
                    .body(request.getBody())
                    .priority(request.getPriority())
                    .correlationId(correlationId)
                    .retryCount(0)
                    .build();

            if (currentCount > RATE_LIMIT_MAX) {
                log.warn("Rate limit exceeded for user: {}", request.getUserId());
                entity.setStatus(NotificationStatus.RATE_LIMITED);
                entity.setErrorMessage("Rate limit exceeded: max " + RATE_LIMIT_MAX + " per hour");
                notificationRepository.save(entity);
                results.add(entity);
                continue;
            }

            entity.setStatus(NotificationStatus.PENDING);
            notificationRepository.save(entity);

            NotificationEvent event = NotificationEvent.builder()
                    .id(entity.getId())
                    .userId(request.getUserId())
                    .channel(channel)
                    .templateId(request.getTemplateId())
                    .variables(request.getVariables())
                    .priority(request.getPriority())
                    .subject(request.getSubject())
                    .body(request.getBody())
                    .correlationId(correlationId)
                    .status(NotificationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            String topic = resolveKafkaTopic(channel);
            ProducerRecord<String, NotificationEvent> record =
                    new ProducerRecord<>(topic, entity.getId(), event);
            record.headers().add(new RecordHeader("correlationId",
                    correlationId.getBytes(StandardCharsets.UTF_8)));
            record.headers().add(new RecordHeader("channel",
                    channel.name().getBytes(StandardCharsets.UTF_8)));

            kafkaTemplate.send(record);
            log.info("Published notification {} to topic {} for user {}", entity.getId(), topic, request.getUserId());

            results.add(entity);
        }

        return results;
    }

    private String resolveKafkaTopic(Channel channel) {
        return switch (channel) {
            case EMAIL -> "notifications.email";
            case SMS   -> "notifications.sms";
            case PUSH  -> "notifications.push";
        };
    }
}
