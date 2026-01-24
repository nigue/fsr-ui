package com.tapir.fsr.service;

import com.fazecast.jSerialComm.SerialPort;
import com.tapir.fsr.config.SerialConfig;
import com.tapir.fsr.event.SensorDataEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class SerialService {

    private static final Logger logger = LoggerFactory.getLogger(SerialService.class);

    @Autowired
    private SerialConfig serialConfig;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${fsr.serial.enabled:true}")
    private boolean serialEnabled;

    @Autowired
    private SensorService sensorService;

    private SerialPort serialPort;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final BlockingQueue<String> writeQueue = new LinkedBlockingQueue<>();
    private Thread readerThread;
    private Thread writerThread;

    @PostConstruct
    public void init() {
        if (serialEnabled) {
            connect();
            startReaderThread();
            startWriterThread();
        } else {
            logger.info("Modo sin dispositivo serial - comunicación deshabilitada");
        }
    }

    @PreDestroy
    public void destroy() {
        running.set(false);
        if (readerThread != null) readerThread.interrupt();
        if (writerThread != null) writerThread.interrupt();
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }
    }

    private void connect() {
        serialPort = serialConfig.serialPort();
        if (serialPort != null) {
            boolean success = serialPort.openPort();
            if (success) {
                logger.info("Conectado al puerto serial: {}", serialPort.getSystemPortName());
                running.set(true);
            } else {
                logger.error("No se pudo abrir el puerto serial");
            }
        } else {
            logger.error("No se encontró ningún puerto serial disponible");
        }
    }

    @Async
    public void startReaderThread() {
        readerThread = new Thread(() -> {
            while (running.get()) {
                if (serialPort == null || !serialPort.isOpen()) {
                    try {
                        Thread.sleep(1000);
                        connect();
                        continue;
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                try {
                    writeQueue.put("v\n");

                    byte[] readBuffer = new byte[64];
                    int numRead = serialPort.readBytes(readBuffer, readBuffer.length);
                    if (numRead > 0) {
                        String response = new String(readBuffer, 0, numRead).trim();
                        processResponse(response);
                    }

                    Thread.sleep(10);
                } catch (Exception e) {
                    logger.error("Error en lectura serial", e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }
        });
        readerThread.start();
    }

    @Async
    public void startWriterThread() {
        writerThread = new Thread(() -> {
            while (running.get()) {
                try {
                    String command = writeQueue.take();
                    if (serialPort != null && serialPort.isOpen()) {
                        serialPort.writeBytes(command.getBytes(), command.length());
                    }
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    logger.error("Error en escritura serial", e);
                }
            }
        });
        writerThread.start();
    }

    private void processResponse(String response) {
        String[] parts = response.split("\\s+");
        if (parts.length < 2) return;

        String command = parts[0];

        switch (command) {
            case "v":
                // Valores de sensores: v val1 val2 val3 val4
                if (parts.length >= 5) {
                    int[] values = new int[4];
                    for (int i = 0; i < 4; i++) {
                        values[i] = Integer.parseInt(parts[i + 1]);
                    }
                    sensorService.processRawValues(values);
                }
                break;
            case "t":
                // Umbrales actuales: t thresh1 thresh2 thresh3 thresh4
                if (parts.length >= 5) {
                    int[] thresholds = new int[4];
                    for (int i = 0; i < 4; i++) {
                        thresholds[i] = Integer.parseInt(parts[i + 1]);
                    }
                    eventPublisher.publishEvent(new SensorDataEvent(this, new Object[]{"thresholds", thresholds}));
                }
                break;
            case "s":
                // Confirmación de guardado
                eventPublisher.publishEvent(new SensorDataEvent(this, new Object[]{"thresholds_persisted", new Object[]{}}));
                break;
        }
    }

    public void updateThreshold(int sensorIndex, int value) {
        String command = String.format("%d %d\n", sensorIndex, value);
        writeQueue.offer(command);
    }

    public void saveThresholds() {
        writeQueue.offer("s\n");
    }

    public void requestThresholds() {
        writeQueue.offer("t\n");
    }

    public void calibrateOffsets() {
        writeQueue.offer("o\n");
    }
}