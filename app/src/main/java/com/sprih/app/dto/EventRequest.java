package com.sprih.app.dto;

import com.sprih.app.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class EventRequest {
    @NotNull(message = "eventType cannot be null")
    private EventType eventType;

    @NotNull(message = "payload cannot be null")
    private Map<String, Object> payload;

    @NotBlank(message = "callbackUrl cannot be blank")
    private String callbackUrl;
}
