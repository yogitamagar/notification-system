package com.sprih.app.processor;

import com.sprih.app.enums.EventType;
import com.sprih.app.model.Event;
import com.sprih.app.client.CallbackClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class QueueManager {

    private static final Logger logger = LoggerFactory.getLogger(QueueManager.class);
    private final Map<EventType, BlockingQueue<Event>> queues = new EnumMap<>(EventType.class);
    private final Map<EventType, EventProcessor> processors = new EnumMap<>(EventType.class);
    private final ExecutorService executorService;
    private final CallbackClient callbackClient;
    private final AtomicBoolean acceptingEvents = new AtomicBoolean(true);


    public QueueManager(CallbackClient callbackClient) {
        this.callbackClient = callbackClient;
        this.executorService = Executors.newFixedThreadPool(EventType.values().length);
    }

    @PostConstruct
    private void init() {
        for (EventType type : EventType.values()) {
            BlockingQueue<Event> queue = new LinkedBlockingQueue<>();
            queues.put(type, queue);
            EventProcessor processor = new EventProcessor(type, queue, callbackClient);
            processors.put(type, processor);
            executorService.submit(processor);
        }
    }

    public void addEvent(Event event) {
        if (!acceptingEvents.get()) {
            throw new IllegalStateException("System is shutting down and not accepting new events.");
        }
        BlockingQueue<Event> queue = queues.get(event.getEventType());
        if (queue != null) {
            try {
                queue.put(event);
                logger.info("Event {} added to {} queue. Queue size: {}", event.getEventId(), event.getEventType(), queue.size());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Failed to add event to queue", e);
            }
        } else {
            throw new IllegalArgumentException("No queue found for event type: " + event.getEventType());
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutdown initiated. Stopping new event acceptance.");
        acceptingEvents.set(false);

        processors.values().forEach(EventProcessor::stop);

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.warn("Executor did not terminate in the specified time.");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Shutdown was interrupted.", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("All event queues processed. Shutdown complete.");
    }
}
