package com.sprih.app.model;

import com.sprih.app.enums.EventType;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class Event {
    private final String eventId;
    private final EventType eventType;
    private final Map<String, Object> payload;
    private final String callbackUrl;

    public Event(EventType eventType, Map<String, Object> payload, String callbackUrl) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.payload = payload;
        this.callbackUrl = callbackUrl;
    }
}
