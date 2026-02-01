package com.tapir.fsr.controller;

import com.fazecast.jSerialComm.SerialPort;
import com.tapir.fsr.data.repository.FsrGetThresholdsRepository;
import com.tapir.fsr.data.repository.FsrSaveThresholdsRepository;
import com.tapir.fsr.data.repository.FsrSetThresholdRepository;
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
    private final FsrSaveThresholdsRepository fsrSaveThresholdsRepository;
    private final FsrSetThresholdRepository fsrSetThresholdRepository;

    public TestController(FsrGetThresholdsRepository fsrGetThresholdsRepository,
                          FsrSaveThresholdsRepository fsrSaveThresholdsRepository,
                          FsrSetThresholdRepository fsrSetThresholdRepository) {
        this.fsrGetThresholdsRepository = fsrGetThresholdsRepository;
        this.fsrSaveThresholdsRepository = fsrSaveThresholdsRepository;
        this.fsrSetThresholdRepository = fsrSetThresholdRepository;
    }

    @GetMapping
    public ResponseEntity<String> test() {
        LOGGER.info("Test endpoint hit");

        SerialPort[] ports = SerialPort.getCommPorts();
        LOGGER.info("Cantidad de puertos seriales: {}", ports.length);

        for (SerialPort port : ports) {
            LOGGER.info("ejecucion");
            //var value = fsrSetThresholdRepository.set(port, 0, 500);
            //var value = fsrSaveThresholdsRepository.save(port);
            var value = fsrGetThresholdsRepository.getAll(port);
            LOGGER.info("valor: {}", value);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            LOGGER.info("{} | SN={} | VID={} PID={}",
                    port.getSystemPortName(),
                    port.getSerialNumber(),
                    port.getVendorID(),
                    port.getProductID());
        }
        return ResponseEntity.ok("test3");
    }


}
