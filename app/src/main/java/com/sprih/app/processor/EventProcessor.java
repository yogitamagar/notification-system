package com.sprih.app.processor;



import com.sprih.app.dto.CallbackRequest;
import com.sprih.app.enums.EventType;
import com.sprih.app.model.Event;
import com.sprih.app.client.CallbackClient;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
@Slf4j
public class EventProcessor implements Runnable {

    private final EventType eventType;
    private final BlockingQueue<Event> queue;
    private final CallbackClient callbackClient;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private static final double FAILURE_RATE = 0.1; // 10% failure rate

    public EventProcessor(EventType eventType, BlockingQueue<Event> queue, CallbackClient callbackClient) {
        this.eventType = eventType;
        this.queue = queue;
        this.callbackClient = callbackClient;
    }

    @Override
    public void run() {
        log.info("Starting processor for {}", eventType);
        while (running.get() || !queue.isEmpty()) {
            try {
                Event event = queue.poll(1, TimeUnit.SECONDS);
                if (event != null) {
                    processEvent(event);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Processor for {} interrupted.", eventType);
                break;
            }
        }
        log.info("Processor for {} shut down gracefully.", eventType);
    }

    private void processEvent(Event event) {
        log.info("Processing event {}: {}", event.getEventType(), event.getEventId());
        try {
            Thread.sleep(eventType.getProcessingTime());

            if (Math.random() < FAILURE_RATE) {
                throw new RuntimeException("Simulated processing failure");
            }

            log.info("Completed event {}: {}", event.getEventType(), event.getEventId());
            sendCompletionCallback(event, "COMPLETED", null);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Processing interrupted for event {}: {}", event.getEventType(), event.getEventId());
            sendCompletionCallback(event, "FAILED", "Processing was interrupted");
        } catch (Exception e) {
            log.error("Failed to process event {}: {}", event.getEventType(), event.getEventId(), e);
            sendCompletionCallback(event, "FAILED", e.getMessage());
        }
    }

    private void sendCompletionCallback(Event event, String status, String errorMessage) {
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .eventId(event.getEventId())
                .status(status)
                .eventType(event.getEventType())
                .errorMessage(errorMessage)
                .processedAt(Instant.now())
                .build();
        callbackClient.sendCallback(event.getCallbackUrl(), callbackRequest);
    }

    public void stop() {
        running.set(false);
        log.info("Stopping processor for {}", eventType);
    }
}
