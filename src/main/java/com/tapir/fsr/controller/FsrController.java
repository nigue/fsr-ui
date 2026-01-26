package com.tapir.fsr.controller;

import com.tapir.fsr.model.SensorUpdate;
import com.tapir.fsr.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;

@Controller
public class FsrController {

    private final ProfileService profileService;
    private final SimpMessagingTemplate messagingTemplate;

    // estado simple en memoria
    private final AtomicIntegerArray sensorValues = new AtomicIntegerArray(4);
    private final AtomicIntegerArray thresholds = new AtomicIntegerArray(new int[]{100,100,100,100});

    @Autowired
    public FsrController(ProfileService profileService, SimpMessagingTemplate messagingTemplate) {
        this.profileService = profileService;
        this.messagingTemplate = messagingTemplate;
        // valores iniciales (ejemplo)
        sensorValues.set(0, 0);
        sensorValues.set(1, 0);
        sensorValues.set(2, 0);
        sensorValues.set(3, 0);
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("profiles", profileService.getAll());
        model.addAttribute("selectedProfile", profileService.getSelectedProfile());
        List<Integer> values = Arrays.asList(
                sensorValues.get(0), sensorValues.get(1),
                sensorValues.get(2), sensorValues.get(3));
        List<Integer> ths = Arrays.asList(
                thresholds.get(0), thresholds.get(1),
                thresholds.get(2), thresholds.get(3));
        model.addAttribute("sensorValues", values);
        model.addAttribute("thresholds", ths);
        return "index";
    }

    @PostMapping("/profile/select")
    public String selectProfile(@RequestParam String profileName) {
        profileService.setSelectedProfile(profileName);
        return "redirect:/";
    }

    @PostMapping("/sensor/{id}/threshold")
    public String setThreshold(@PathVariable int id, @RequestParam int threshold) {
        if (id >=0 && id < thresholds.length()) {
            thresholds.set(id, threshold);
            // notificar a clientes vía websocket
            SensorUpdate u = new SensorUpdate(id, sensorValues.get(id), threshold);
            messagingTemplate.convertAndSend("/topic/sensor", u);
        }
        return "redirect:/";
    }

    // Método auxiliar que otros servicios (por ejemplo ArduinoService) deben llamar
    public void publishSensor(int id, int value) {
        if (id >=0 && id < sensorValues.length()) {
            sensorValues.set(id, value);
            int th = thresholds.get(id);
            SensorUpdate u = new SensorUpdate(id, value, th);
            messagingTemplate.convertAndSend("/topic/sensor", u);
        }
    }
}
