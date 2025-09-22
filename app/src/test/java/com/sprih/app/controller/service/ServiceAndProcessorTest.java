package com.sprih.app.controller.service;

import com.sprih.app.client.CallbackClient;
import com.sprih.app.dto.CallbackRequest;
import com.sprih.app.enums.EventType;
import com.sprih.app.model.Event;
import com.sprih.app.processor.EventProcessor;
import com.sprih.app.processor.QueueManager;
import com.sprih.app.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceAndProcessorTest {

    @Mock
    private CallbackClient callbackClient;

    @Captor
    private ArgumentCaptor<CallbackRequest> callbackRequestCaptor;

    private QueueManager queueManager;
    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        queueManager = new QueueManager(callbackClient);
        eventService = new EventServiceImpl(queueManager);
    }

    @Test
    void eventProcessor_processesEventAndSendsSuccessCallback() throws InterruptedException {
        BlockingQueue<Event> queue = new LinkedBlockingQueue<>();
        Event event = new Event(EventType.SMS, Map.of("phone", "123"), "http://callback.url");
        queue.put(event);

        // Mocking Math.random() is tricky, so we'll assume success for this test
        EventProcessor processor = new EventProcessor(EventType.SMS, queue, callbackClient);

        // Run in a separate thread to not block the test
        Thread processorThread = new Thread(processor);
        processorThread.start();

        // Wait for the event to be processed
        Thread.sleep(EventType.SMS.getProcessingTime() + 500);
        processor.stop();
        processorThread.join();

        verify(callbackClient).sendCallback(eq("http://callback.url"), callbackRequestCaptor.capture());
        CallbackRequest captured = callbackRequestCaptor.getValue();
        assertEquals("COMPLETED", captured.getStatus());
        assertEquals(event.getEventId(), captured.getEventId());
    }

    @Test
    void queueManager_shouldStopAcceptingEventsOnShutdown() {
        QueueManager realQueueManager = new QueueManager(callbackClient);

        realQueueManager.shutdown();

        Event event = new Event(EventType.EMAIL, Map.of("to", "test@test.com"), "url");
        assertThrows(IllegalStateException.class, () -> realQueueManager.addEvent(event));
    }

}
