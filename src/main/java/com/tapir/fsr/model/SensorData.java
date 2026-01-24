package com.tapir.fsr.model;

public record SensorData(int sensorId,
                         int rawValue,
                         int smoothedValue,
                         int threshold,
                         boolean isActive) {
}
