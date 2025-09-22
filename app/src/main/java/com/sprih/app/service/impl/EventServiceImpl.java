package com.sprih.app.service.impl;

import com.sprih.app.model.Event;
import com.sprih.app.processor.QueueManager;
import com.sprih.app.service.EventService;
import org.springframework.stereotype.Service;

@Service
public class EventServiceImpl implements EventService {

    private final QueueManager queueManager;

    public EventServiceImpl(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @Override
    public Event acceptEvent(Event event) {
        queueManager.addEvent(event);
        return event;
    }
}
