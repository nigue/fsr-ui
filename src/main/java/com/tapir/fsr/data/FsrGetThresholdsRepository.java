package com.tapir.fsr.data;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class FsrGetThresholdsRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(FsrGetThresholdsRepository.class);

    public String getAll(SerialPort port) {
        port.setComPortParameters(
                9600,                       // ← baud correcto
                8,
                SerialPort.ONE_STOP_BIT,
                SerialPort.NO_PARITY
        );

        port.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING, // ← no cuelga
                300,
                0
        );

        if (!port.openPort()) {
            LOGGER.warn("No se pudo abrir el puerto {}", port.getSystemPortName());
            return "NO_OPEN";
        }

        try {
            // Teensy necesita estabilizar USB
            Thread.sleep(50);

            // Limpia basura previa (ráfagas, botones, etc.)
            //port.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);

            // Envía comando
            port.writeBytes(FsrRequestCommand.THRESHOLDS.toBytes(), 1);

            // Lee respuesta
            byte[] buffer = new byte[128];
            int n = port.readBytes(buffer, buffer.length);

            if (n <= 0) {
                return "NO_RESPONSE";
            }

            return new String(buffer, 0, n).trim();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "INTERRUPTED";
        } finally {
            port.closePort();
        }
    }
}
