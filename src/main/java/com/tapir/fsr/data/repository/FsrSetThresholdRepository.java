package com.tapir.fsr.data.repository;

import com.fazecast.jSerialComm.SerialPort;
import com.tapir.fsr.data.exception.SerialCommunicationException;
import com.tapir.fsr.data.exception.SerialPortOpenException;
import com.tapir.fsr.data.exception.SerialTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;

@Repository
public class FsrSetThresholdRepository {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FsrSetThresholdRepository.class);

    public String set(
            SerialPort port,
            int sensorIndex,
            int threshold
    ) {

        if (sensorIndex < 0 || sensorIndex > 7) {
            throw new IllegalArgumentException(
                    "sensorIndex fuera de rango: " + sensorIndex
            );
        }

        if (threshold < 0 || threshold > 1023) {
            throw new IllegalArgumentException(
                    "threshold fuera de rango: " + threshold
            );
        }

        LOGGER.info(
                "Configurando threshold sensor={} valor={}",
                sensorIndex,
                threshold
        );

        port.setComPortParameters(
                9600,
                8,
                SerialPort.ONE_STOP_BIT,
                SerialPort.NO_PARITY
        );

        port.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                300,
                0
        );

        if (!port.openPort()) {
            LOGGER.error("No se pudo abrir el puerto {}", port.getSystemPortName());
            throw new SerialPortOpenException(
                    "No se pudo abrir el puerto " + port.getSystemPortName()
            );
        }

        try {
            LOGGER.info("Puerto {} abierto", port.getSystemPortName());

            Thread.sleep(50); // USB settle (Teensy)

            String command = sensorIndex + " " + threshold + "\n";
            byte[] cmdBytes = command.getBytes(StandardCharsets.US_ASCII);

            LOGGER.info("Enviando comando: [{}]", command.trim());
            port.writeBytes(cmdBytes, cmdBytes.length);

            byte[] buffer = new byte[128];
            int n = port.readBytes(buffer, buffer.length);

            if (n <= 0) {
                LOGGER.error("Timeout esperando respuesta del sensor");
                throw new SerialTimeoutException(
                        "Timeout esperando respuesta al setThreshold"
                );
            }

            String response = new String(buffer, 0, n, StandardCharsets.US_ASCII)
                    .trim();

            LOGGER.info("Respuesta recibida: [{}]", response);

            return response;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Comunicación serial interrumpida", e);
            throw new SerialCommunicationException(
                    "Comunicación serial interrumpida",
                    e
            );

        } catch (Exception e) {
            LOGGER.error(
                    "Error comunicándose con {}",
                    port.getSystemPortName(),
                    e
            );
            throw new SerialCommunicationException(
                    "Error comunicándose con la placa",
                    e
            );

        } finally {
            port.closePort();
            LOGGER.info("Puerto {} cerrado", port.getSystemPortName());
        }
    }
}
