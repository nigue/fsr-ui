package com.tapir.fsr.handler;

import com.tapir.fsr.event.SensorDataEvent;
import com.tapir.fsr.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class FsrWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FsrWebSocketHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    private ProfileService profileService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);

        broadcast(new Object[]{"thresholds", profileService.getCurrentProfile().thresholds()});
        broadcast(new Object[]{"get_profiles", profileService.getAllProfiles().stream()
                .map(p -> p.name()).toList()});
        broadcast(new Object[]{"get_cur_profile", profileService.getCurrentProfile().name()});
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            Object[] data = objectMapper.readValue(message.getPayload(), Object[].class);
            String action = (String) data[0];

            switch (action) {
                case "update_threshold":
                    int[] values = (int[]) data[1];
                    int index = (int) data[2];
                    // Enviar comando serial vía evento o servicio separado
                    break;
                case "save_thresholds":
                    // Lógica de guardado
                    break;
                case "add_profile":
                    String profileName = (String) data[1];
                    int[] thresholds = (int[]) data[2];
                    profileService.addProfile(profileName,
                            java.util.Arrays.stream(thresholds).boxed().toList());
                    broadcast(new Object[]{"get_profiles", profileService.getAllProfiles().stream()
                            .map(p -> p.name()).toList()});
                    break;
                case "remove_profile":
                    String removeProfileName = (String) data[1];
                    profileService.removeProfile(removeProfileName);
                    broadcast(new Object[]{"get_profiles", profileService.getAllProfiles().stream()
                            .map(p -> p.name()).toList()});
                    break;
                case "change_profile":
                    String changeProfileName = (String) data[1];
                    profileService.changeProfile(changeProfileName);
                    broadcast(new Object[]{"get_cur_profile", changeProfileName});
                    break;
            }
        } catch (Exception e) {
            broadcast(new Object[]{"error", "Invalid message format"});
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
    }

    @EventListener
    public void handleSensorDataEvent(SensorDataEvent event) {
        broadcast(event.getData());
    }

    public void broadcast(Object message) {
        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(message);
            sessions.values().forEach(session -> {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(jsonMessage));
                    } catch (Exception e) {
                        LOGGER.warn("Session {} is not open", session.getId());
                        sessions.remove(session.getId());
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error("Can not find a session");
            // Manejar error de serialización
            throw new RuntimeException("Can not find a session");
        }
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }

    public java.util.Set<String> getActiveSessionIds() {
        return sessions.keySet();
    }

}