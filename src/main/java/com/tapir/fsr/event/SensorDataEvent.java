package com.tapir.fsr.event;

import org.springframework.context.ApplicationEvent;

public class SensorDataEvent extends ApplicationEvent {
    private final Object[] data;

    public SensorDataEvent(Object source, Object[] data) {
        super(source);
        this.data = data;
    }

    public Object[] getData() {
        return data;
    }
}