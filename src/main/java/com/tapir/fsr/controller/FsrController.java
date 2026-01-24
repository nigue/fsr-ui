package com.tapir.fsr.controller;

import com.tapir.fsr.model.Profile;
import com.tapir.fsr.service.ProfileService;
import com.tapir.fsr.service.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FsrController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private SensorService sensorService;

    @GetMapping("/defaults")
    public ResponseEntity<Map<String, Object>> getDefaults() {
        Map<String, Object> response = new HashMap<>();
        response.put("profiles", profileService.getAllProfiles().stream()
                .map(Profile::name).toList());
        response.put("cur_profile", profileService.getCurrentProfile().name());
        response.put("thresholds", profileService.getCurrentProfile().thresholds());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profiles")
    public ResponseEntity<List<Profile>> getAllProfiles() {
        return ResponseEntity.ok(profileService.getAllProfiles());
    }

    @PostMapping("/profiles")
    public ResponseEntity<Profile> createProfile(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        @SuppressWarnings("unchecked")
        List<Integer> thresholds = (List<Integer>) request.get("thresholds");

        Profile created = profileService.addProfile(name, thresholds);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/profiles/{name}")
    public ResponseEntity<Profile> updateProfile(
            @PathVariable String name,
            @RequestBody Map<String, Object> request) {

        @SuppressWarnings("unchecked")
        List<Integer> thresholds = (List<Integer>) request.get("thresholds");
        profileService.updateThresholds(name, thresholds);

        return ResponseEntity.ok(profileService.findByName(name).orElse(null));
    }

    @DeleteMapping("/profiles/{name}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String name) {
        profileService.removeProfile(name);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sensors")
    public ResponseEntity<Map<String, Object>> getSensorData() {
        Map<String, Object> response = new HashMap<>();
        response.put("values", sensorService.getCurrentValues());
        response.put("thresholds", sensorService.getCurrentThresholds());
        response.put("states", sensorService.getSensorStates());
        response.put("activeCount", sensorService.getActiveSensorCount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/profiles/{name}/activate")
    public ResponseEntity<Void> activateProfile(@PathVariable String name) {
        profileService.changeProfile(name);
        return ResponseEntity.ok().build();
    }
}
