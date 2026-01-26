package com.tapir.fsr.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProfileService {
    private final List<Profile> profiles = new ArrayList<>();
    private String selectedProfile;

    public ProfileService() {
        // Crear perfiles en el constructor (ejemplo simple)
        profiles.add(new Profile("Default"));
        profiles.add(new Profile("Sensitivo"));
        profiles.add(new Profile("Robusto"));

        selectedProfile = profiles.get(0).getName();
    }

    public List<Profile> getAll() { return profiles; }

    public void add(String name) {
        profiles.add(new Profile(name));
    }

    public String getSelectedProfile() { return selectedProfile; }

    public void setSelectedProfile(String name) { this.selectedProfile = name; }

    public void delete(String name) {

    }

    public static class Profile {
        private final String name;
        public Profile(String name) { this.name = name; }
        public String getName() { return name; }
    }
}