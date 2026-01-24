package com.tapir.fsr.service;

import com.tapir.fsr.model.Profile;
import com.tapir.fsr.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    public Optional<Profile> findByName(String name) {
        return profileRepository.findByName(name);
    }

    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }

    public Profile getCurrentProfile() {
        return profileRepository.getCurrentProfile();
    }

    public void changeProfile(String profileName) {
        profileRepository.setCurrentProfile(profileName);
    }

    public Profile addProfile(String name, List<Integer> thresholds) {
        Profile profile = new Profile(name, thresholds, false);
        Profile saved = profileRepository.save(profile);
        profileRepository.setCurrentProfile(name);
        return saved;
    }

    public void removeProfile(String name) {
        profileRepository.deleteByName(name);
        // Si eliminamos el perfil actual, cambiar al perfil por defecto
        if (name.equals(profileRepository.getCurrentProfile().name())) {
            profileRepository.setCurrentProfile("");
        }
    }

    public void updateThresholds(String profileName, List<Integer> thresholds) {
        Profile updatedProfile = new Profile(profileName, thresholds,
                profileName.equals(profileRepository.getCurrentProfile().name()));
        profileRepository.save(updatedProfile);
    }
}
