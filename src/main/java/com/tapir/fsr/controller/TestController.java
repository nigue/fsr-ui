package com.tapir.fsr.controller;

import com.fazecast.jSerialComm.SerialPort;
import com.tapir.fsr.data.FsrGetThresholdsRepository;
import com.tapir.fsr.data.FsrRequestCommand;
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

    private final FsrGetThresholdsRepository fsrGetThresholdsRepository;

    public TestController(FsrGetThresholdsRepository fsrGetThresholdsRepository) {
        this.fsrGetThresholdsRepository = fsrGetThresholdsRepository;
    }

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
                    fsrGetThresholdsRepository.getAll(port)
            );
        }
        return ResponseEntity.ok("test2");
    }


}
