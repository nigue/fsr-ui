package com.tapir.fsr.repository;

import com.tapir.fsr.model.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ProfileRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileRepository.class);

    private final Map<String, Profile> profiles = new ConcurrentHashMap<>();
    private String currentProfile = "user1";

    public ProfileRepository() {
        // Perfiles creados en memoria
        profiles.put("user1", new Profile("user1", Arrays.asList(10, 5, 8, 4), true));
        profiles.put("user2", new Profile("user2", Arrays.asList(12, 6, 9, 5), false));
        profiles.put("guest", new Profile("guest", Arrays.asList(15, 10, 10, 6), false));

        // Asegurar que currentProfile existe en el mapa
        if (!profiles.containsKey(currentProfile) && !profiles.isEmpty()) {
            currentProfile = profiles.keySet().iterator().next();
        }
    }

    public List<Profile> findAll() {
        return new ArrayList<>(profiles.values());
    }

    public Optional<Profile> findByName(String name) {
        return Optional.ofNullable(profiles.get(name));
    }

    public Profile save(Profile profile) {
        profiles.put(profile.name(), profile);
        if (profile.active()) {
            currentProfile = profile.name();
        }
        return profile;
    }

    public void deleteByName(String name) {
        profiles.remove(name);
        if (name != null && name.equals(currentProfile)) {
            // cambiar currentProfile al primero disponible o dejarlo null
            currentProfile = profiles.keySet().stream().findFirst().orElse(null);
        }
    }

    public Profile getCurrentProfile() {
        return profiles.get(currentProfile);
    }

    public void setCurrentProfile(String profileName) {
        if (profiles.containsKey(profileName)) {
            currentProfile = profileName;
        } else {
            LOGGER.warn("Perfil no encontrado: {}", profileName);
        }
    }

    public List<String> getProfileNames() {
        return profiles.keySet().stream()
                .filter(name -> name != null && !name.isEmpty())
                .sorted()
                .collect(Collectors.toList());
    }
}
