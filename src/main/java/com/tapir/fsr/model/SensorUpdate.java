package com.tapir.fsr.model;

public class SensorUpdate {
    private int id;
    private int value;
    private int threshold;

    public SensorUpdate() {}

    public SensorUpdate(int id, int value, int threshold) {
        this.id = id; this.value = value; this.threshold = threshold;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    public int getThreshold() { return threshold; }
    public void setThreshold(int threshold) { this.threshold = threshold; }
}
