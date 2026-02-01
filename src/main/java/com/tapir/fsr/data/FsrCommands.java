package com.tapir.fsr.data;

import java.nio.charset.StandardCharsets;

public class FsrCommands {

    public static byte[] setThreshold(int sensorIndex, int value) {
        if (sensorIndex < 0 || sensorIndex > 9) {
            throw new IllegalArgumentException("Sensor index must be 0..9");
        }
        if (value < 0 || value > 1023) {
            throw new IllegalArgumentException("Threshold must be 0..1023");
        }

        String cmd = sensorIndex + " " + value;
        return cmd.getBytes(StandardCharsets.US_ASCII);
    }
}
