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
public class FsrSaveThresholdsRepository {


    private static final Logger LOGGER =
            LoggerFactory.getLogger(FsrSaveThresholdsRepository.class);

    public String save(SerialPort port) {

        LOGGER.info("Solicitando guardado de thresholds en placa");

        port.setComPortParameters(
                115200,
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

            // USB settle (Teensy / CDC)
            Thread.sleep(50);

            LOGGER.info("Enviando comando SAVE (s)");
            port.writeBytes(
                    FsrRequestCommand.SAVE_THRESHOLDS.toBytes(),
                    1
            );

            LOGGER.info("Lectura de resultado al SAVE");
            byte[] buffer = new byte[128];
            int n = port.readBytes(buffer, buffer.length);

            if (n <= 0) {
                throw new SerialTimeoutException(
                        "Timeout esperando confirmación SAVE"
                );
            }

            String response = new String(buffer, 0, n).trim();
            LOGGER.info("Respuesta SAVE recibida: [{}]", response);

            if (!response.startsWith("s ")) {
                throw new SerialCommunicationException(
                        "Respuesta inválida al comando SAVE: " + response,
                        new Throwable()
                );
            }

            return response;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SerialCommunicationException(
                    "Comunicación serial interrumpida",
                    e
            );

        } catch (Exception e) {
            throw new SerialCommunicationException(
                    "Error guardando thresholds en placa",
                    e
            );

        } finally {
            port.closePort();
            LOGGER.info("Puerto {} cerrado", port.getSystemPortName());
        }
    }
}
