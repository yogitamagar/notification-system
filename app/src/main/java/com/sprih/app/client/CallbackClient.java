package com.sprih.app.client;

import com.sprih.app.dto.CallbackRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
public class CallbackClient {

    private final RestTemplate restTemplate;

    public CallbackClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendCallback(String url, CallbackRequest payload) {
        try {
            log.info("Sending callback for eventId: {} to URL: {}",
                    payload.getEventId(),
                    url);
            restTemplate.postForEntity(url, payload, String.class);
            log.info("Successfully sent callback for eventId: {}", payload.getEventId());
        } catch (RestClientException e) {
            log.error("Failed to send callback for eventId: {}. Error: {}", payload.getEventId(), e.getMessage());
        }
    }
}
