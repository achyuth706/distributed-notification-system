package com.nds.shared.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nds.shared.enums.Channel;
import com.nds.shared.enums.NotificationStatus;
import com.nds.shared.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationEvent {

    private String id;
    private String userId;
    private Channel channel;
    private String templateId;
    private Map<String, String> variables;
    private Priority priority;
    private String subject;
    private String body;
    private String correlationId;
    private NotificationStatus status;
    private int retryCount;
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;

    private long processingTimeMs;
}
