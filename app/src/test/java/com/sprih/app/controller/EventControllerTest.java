package com.sprih.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprih.app.dto.EventRequest;
import com.sprih.app.enums.EventType;
import com.sprih.app.model.Event;
import com.sprih.app.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createEvent_whenValidRequest_shouldReturnAccepted() throws Exception {
        EventRequest request = new EventRequest();
        request.setEventType(EventType.EMAIL);
        request.setPayload(Map.of("recipient", "test@example.com"));
        request.setCallbackUrl("http://localhost:9090/callback");

        Event event = new Event(request.getEventType(), request.getPayload(), request.getCallbackUrl());
        when(eventService.acceptEvent(any(Event.class))).thenReturn(event);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.eventId").value(event.getEventId()))
                .andExpect(jsonPath("$.message").value("Event accepted for processing."));
    }

    @Test
    void createEvent_whenEventTypeIsNull_shouldReturnBadRequest() throws Exception {
        EventRequest request = new EventRequest();
        request.setPayload(Map.of("recipient", "test@example.com"));
        request.setCallbackUrl("http://localhost:9090/callback");

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvent_whenPayloadIsNull_shouldReturnBadRequest() throws Exception {
        EventRequest request = new EventRequest();
        request.setEventType(EventType.SMS);
        request.setCallbackUrl("http://localhost:9090/callback");

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvent_whenCallbackUrlIsBlank_shouldReturnBadRequest() throws Exception {
        EventRequest request = new EventRequest();
        request.setEventType(EventType.PUSH);
        request.setPayload(Map.of("deviceId", "123"));
        request.setCallbackUrl("");

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvent_whenShuttingDown_shouldReturnServiceUnavailable() throws Exception {
        EventRequest request = new EventRequest();
        request.setEventType(EventType.EMAIL);
        request.setPayload(Map.of("recipient", "test@example.com"));
        request.setCallbackUrl("http://localhost:9090/callback");

        when(eventService.acceptEvent(any(Event.class))).thenThrow(new IllegalStateException("System is shutting down"));

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable());
    }
}
