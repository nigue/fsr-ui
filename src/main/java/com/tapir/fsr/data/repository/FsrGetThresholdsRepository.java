package com.tapir.fsr.data.repository;

import com.fazecast.jSerialComm.SerialPort;
import com.tapir.fsr.data.FsrRequestCommand;
import com.tapir.fsr.data.exception.SerialCommunicationException;
import com.tapir.fsr.data.exception.SerialPortOpenException;
import com.tapir.fsr.data.exception.SerialTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class FsrGetThresholdsRepository {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FsrGetThresholdsRepository.class);

    public String getAll(SerialPort port) {

        LOGGER.info("Configurando puerto {}", port.getSystemPortName());

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
            LOGGER.info("Puerto {} abierto correctamente", port.getSystemPortName());

            // Teensy USB settle
            Thread.sleep(50);

            LOGGER.info("Enviando comando THRESHOLDS");
            port.writeBytes(FsrRequestCommand.THRESHOLDS.toBytes(), 1);

            byte[] buffer = new byte[128];
            int n = port.readBytes(buffer, buffer.length);

            if (n <= 0) {
                LOGGER.error(
                        "Timeout leyendo thresholds desde {}",
                        port.getSystemPortName()
                );
                throw new SerialTimeoutException(
                        "Timeout esperando respuesta THRESHOLDS"
                );
            }

            String response = new String(buffer, 0, n).trim();
            LOGGER.info("Respuesta recibida: [{}]", response);

            return response;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Hilo interrumpido durante comunicación serial", e);
            throw new SerialCommunicationException(
                    "Comunicación serial interrumpida",
                    e
            );

        } catch (Exception e) {
            LOGGER.error(
                    "Error inesperado comunicándose con {}",
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
