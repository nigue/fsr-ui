package com.tapir.fsr.repository;

import com.tapir.fsr.model.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ProfileRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileRepository.class);

    private final Map<String, Profile> profiles = new ConcurrentHashMap<>();
    private String currentProfile = "user1";
    private final Path profilesPath;

    public ProfileRepository() {
        this.profilesPath = Paths.get("profiles.txt");
        loadProfiles();
    }

    public List<Profile> findAll() {
        return new ArrayList<>(profiles.values());
    }

    public Optional<Profile> findByName(String name) {
        return Optional.ofNullable(profiles.get(name));
    }

    public Profile save(Profile profile) {
        profiles.put(profile.name(), profile);
        saveToFile();
        return profile;
    }

    public void deleteByName(String name) {
        profiles.remove(name);
        saveToFile();
    }

    public Profile getCurrentProfile() {
        return profiles.get(currentProfile);
    }

    public void setCurrentProfile(String profileName) {
        if (profiles.containsKey(profileName)) {
            this.currentProfile = profileName;
        }
    }

    public List<String> getProfileNames() {
        return profiles.keySet().stream()
                .filter(name -> !name.isEmpty())
                .sorted()
                .toList();
    }

    private void loadProfiles() {
        // Perfil por defecto
        profiles.put("user1", new Profile("user1", Arrays.asList(10, 5, 8, 4), true));

        if (!Files.exists(profilesPath)) {
            try {
                Files.createFile(profilesPath);
            } catch (IOException e) {
                // Manejar error
            }
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(profilesPath)) {
            String line;
            boolean firstProfile = true;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 5) { // nombre + 4 umbrales
                    String name = parts[0];
                    List<Integer> thresholds = Arrays.stream(parts, 1, parts.length)
                            .map(Integer::parseInt)
                            .toList();
                    Profile profile = new Profile(name, thresholds, name.equals(currentProfile));
                    profiles.put(name, profile);

                    if (firstProfile) {
                        currentProfile = name;
                        firstProfile = false;
                    }
                } else {
                    LOGGER.error("Formato inválido en profiles.txt: {}", line);
                }
            }
        } catch (IOException e) {
            // Manejar error
        }
    }

    private void saveToFile() {
        try (BufferedWriter writer = Files.newBufferedWriter(profilesPath)) {
            for (Profile profile : profiles.values()) {
                if (!profile.name().isEmpty()) {
                    writer.write(String.format("%s %s%n",
                            profile.name(),
                            profile.thresholds().stream()
                                    .map(String::valueOf)
                                    .reduce((a, b) -> a + " " + b)
                                    .orElse("")
                    ));
                }
            }
        } catch (IOException e) {
            // Manejar error
        }
    }
}
