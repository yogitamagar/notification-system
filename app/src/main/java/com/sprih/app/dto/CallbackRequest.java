package com.sprih.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sprih.app.enums.EventType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CallbackRequest {
    private String eventId;
    private String status;
    private EventType eventType;
    private String errorMessage;
    private Instant processedAt;
}

