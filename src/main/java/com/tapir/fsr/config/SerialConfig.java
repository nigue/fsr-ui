package com.tapir.fsr.config;

import com.fazecast.jSerialComm.SerialPort;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fsr.serial")
public class SerialConfig {

    private String port = "/dev/ttyACM0";
    private int baudRate = 115200;
    private int timeout = 1000;
    private int numSensors = 4;

    @Bean
    public SerialPort serialPort() {
        SerialPort[] ports = SerialPort.getCommPorts();
        SerialPort selectedPort = null;

        // Buscar el puerto configurado o usar el primero disponible
        for (SerialPort port : ports) {
            if (port.getSystemPortName().equals(this.port) ||
                    port.getDescriptivePortName().contains("Arduino") ||
                    port.getDescriptivePortName().contains("Teensy")) {
                selectedPort = port;
                break;
            }
        }

        if (selectedPort == null && ports.length > 0) {
            selectedPort = ports[0];
        }

        if (selectedPort != null) {
            selectedPort.setBaudRate(baudRate);
            selectedPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, timeout, 0);
        }

        return selectedPort;
    }

    // Getters y Setters
    public String getPort() { return port; }
    public void setPort(String port) { this.port = port; }

    public int getBaudRate() { return baudRate; }
    public void setBaudRate(int baudRate) { this.baudRate = baudRate; }

    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }

    public int getNumSensors() { return numSensors; }
    public void setNumSensors(int numSensors) { this.numSensors = numSensors; }
}