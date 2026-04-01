package com.nds.gateway.dto;

import com.nds.shared.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String userId;
    private List<String> channels;
    private String templateId;
    private Map<String, String> variables;

    @Builder.Default
    private Priority priority = Priority.NORMAL;

    private String subject;
    private String body;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
