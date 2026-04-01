package com.nds.gateway.dto;

import com.nds.shared.enums.NotificationStatus;
import com.nds.shared.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String userId;
    private String channel;
    private NotificationStatus status;
    private Priority priority;
    private String subject;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private String correlationId;
    private String errorMessage;
}
