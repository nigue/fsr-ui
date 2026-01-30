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
        port.setComPortParameters(115200, 8,
                SerialPort.ONE_STOP_BIT,
                SerialPort.NO_PARITY);

        port.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_BLOCKING,
                1000,
                0
        );

        if (!port.openPort()) {
            LOGGER.warn("No se pudo abrir el puerto {}", port.getSystemPortName());
            return "NO_OPEN";
        }

        try {
            byte[] cmd = "t".getBytes();
            port.writeBytes(cmd, cmd.length);

            byte[] buffer = new byte[64];
            int n = port.readBytes(buffer, buffer.length);

            if (n <= 0) {
                return "NO_RESPONSE";
            }

            return new String(buffer, 0, n).trim();

        } finally {
            port.closePort();
        }
    }

}
