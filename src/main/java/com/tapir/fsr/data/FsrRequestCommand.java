package com.tapir.fsr.data;

public enum FsrRequestCommand {

    VALUES('v', true),
    THRESHOLDS('t', true),
    SAVE_THRESHOLDS('s', true),
    CALIBRATE_OFFSET('o', false);

    private final char code;
    private final boolean expectsResponse;

    FsrRequestCommand(char code, boolean expectsResponse) {
        this.code = code;
        this.expectsResponse = expectsResponse;
    }

    public char getCode() {
        return code;
    }

    public boolean expectsResponse() {
        return expectsResponse;
    }

    public byte[] toBytes() {
        return new byte[]{ (byte) code };
    }
}
