package com.nds.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dead_letter_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeadLetterEvent {

    @Id
    private String id;

    @Column(name = "notification_id")
    private String notificationId;

    @Column(name = "topic")
    private String topic;

    @Column(name = "partition_id")
    private Integer partitionId;

    @Column(name = "offset_value")
    private Long offsetValue;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    @Builder.Default
    private int retryCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
