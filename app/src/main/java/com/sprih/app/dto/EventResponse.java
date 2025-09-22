package com.sprih.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventResponse {
    private String eventId;
    private String message;
}
