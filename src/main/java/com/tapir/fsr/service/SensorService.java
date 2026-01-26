package com.tapir.fsr.service;

import com.tapir.fsr.event.SensorDataEvent;
import com.tapir.fsr.model.SensorData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SensorService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ProfileService profileService;

    private final AtomicReference<int[]> currentValues = new AtomicReference<>(new int[4]);
    private final AtomicReference<int[]> currentThresholds = new AtomicReference<>(new int[4]);
    private final AtomicReference<boolean[]> sensorStates = new AtomicReference<>(new boolean[4]);

    public void processRawValues(int[] rawValues) {
        if (rawValues == null || rawValues.length != 4) {
            return;
        }

        // Actualizar valores actuales
        currentValues.set(rawValues.clone());

        // Obtener umbrales del perfil actual
        int[] thresholds = new int[]{1,2,3,4};
        currentThresholds.set(thresholds.clone());

        // Evaluar estados de sensores
        boolean[] states = new boolean[4];
        List<SensorData> sensorDataList = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            boolean isActive = rawValues[i] >= thresholds[i];
            states[i] = isActive;

            sensorDataList.add(new SensorData(
                    i + 1,                    // sensorId (1-based)
                    rawValues[i],             // rawValue
                    rawValues[i],             // smoothedValue (sin suavizado en backend)
                    thresholds[i],            // threshold
                    isActive                  // active
            ));
        }

        sensorStates.set(states);

        // Publicar evento con datos procesados
        eventPublisher.publishEvent(new SensorDataEvent(this, new Object[]{"values", rawValues}));

        // Si algún sensor cambió de estado, publicar evento de estados
        eventPublisher.publishEvent(new SensorDataEvent(this, new Object[]{"sensor_states", states}));
    }

    public int[] getCurrentValues() {
        return currentValues.get().clone();
    }

    public int[] getCurrentThresholds() {
        return currentThresholds.get().clone();
    }

    public boolean[] getSensorStates() {
        return sensorStates.get().clone();
    }

    public List<SensorData> getAllSensorData() {
        int[] values = currentValues.get();
        int[] thresholds = currentThresholds.get();
        boolean[] states = sensorStates.get();

        List<SensorData> result = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            result.add(new SensorData(
                    i + 1,
                    values[i],
                    values[i],
                    thresholds[i],
                    states[i]
            ));
        }
        return result;
    }

    public void updateThreshold(int sensorIndex, int newThreshold) {
        int[] thresholds = currentThresholds.get();
        if (sensorIndex >= 0 && sensorIndex < thresholds.length) {
            thresholds[sensorIndex] = newThreshold;
            currentThresholds.set(thresholds.clone());

            // Re-evaluar estados con nuevos umbrales
            processRawValues(currentValues.get());
        }
    }

    public boolean isAnySensorActive() {
        boolean[] states = sensorStates.get();
        for (boolean state : states) {
            if (state) return true;
        }
        return false;
    }

    public int getActiveSensorCount() {
        boolean[] states = sensorStates.get();
        int count = 0;
        for (boolean state : states) {
            if (state) count++;
        }
        return count;
    }
}