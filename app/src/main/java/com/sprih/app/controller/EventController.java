package com.sprih.app.controller;

import com.sprih.app.dto.EventRequest;
import com.sprih.app.dto.EventResponse;
import com.sprih.app.model.Event;
import com.sprih.app.service.EventService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private static final Logger logger = LoggerFactory.getLogger(EventController.class);
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@Valid @RequestBody EventRequest eventRequest) {
        try {
            Event newEvent = new Event(
                    eventRequest.getEventType(),
                    eventRequest.getPayload(),
                    eventRequest.getCallbackUrl()
            );

            Event acceptedEvent = eventService.acceptEvent(newEvent);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                    new EventResponse(acceptedEvent.getEventId(), "Event accepted for processing.")
            );
        } catch (IllegalStateException e) {
            logger.warn("Attempted to submit event during shutdown: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
        catch (Exception e) {
            logger.error("Error processing event request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }
}
