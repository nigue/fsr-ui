package com.tapir.fsr.model;

public record SensorConfig(int pinNumber,
                           int threshold,
                           int offset,
                           boolean enabled) {
}
