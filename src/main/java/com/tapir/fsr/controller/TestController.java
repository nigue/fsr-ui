package com.tapir.fsr.controller;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);

    @GetMapping
    public ResponseEntity<String> test() {
        LOGGER.info("Test endpoint hit");

        SerialPort[] ports = SerialPort.getCommPorts();
        LOGGER.info("Cantidad de puertos seriales: {}", ports.length);

        for (SerialPort port : ports) {
            LOGGER.info(
                    "{} | SN={} | VID={} PID={}, NAME={}",
                    port.getSystemPortName(),
                    port.getSerialNumber(),
                    port.getVendorID(),
                    port.getProductID(),
                    findName(port)
            );
        }
        return ResponseEntity.ok("test");
    }

    private String findName(SerialPort port) {
        port.writeBytes("t\n".getBytes(), 4);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        byte[] buffer = new byte[64];
        int n = port.readBytes(buffer, buffer.length);
        String id = new String(buffer, 0, n).trim();

        return id;
    }

}
