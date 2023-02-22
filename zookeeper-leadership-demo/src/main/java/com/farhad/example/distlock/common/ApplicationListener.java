package com.farhad.example.distlock.common;

import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplicationListener {
    
    @EventListener
    public void handleContextRefreshEvent(ContextClosedEvent contextClosedEvent) {
        log.info("CONTEXT CLOSED EVENT: {}" + contextClosedEvent.toString());
    }
}
