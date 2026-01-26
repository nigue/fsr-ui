package com.tapir.fsr.controller;

import com.tapir.fsr.websocket.WebSocketSessionRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/websocket")
public class AdminWebSocketController {

    private final WebSocketSessionRegistry registry;
    private final SimpMessagingTemplate messagingTemplate;

    public AdminWebSocketController(WebSocketSessionRegistry registry, SimpMessagingTemplate messagingTemplate) {
        this.registry = registry;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getWebSocketStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("activeConnections", registry.count());
        response.put("status", "running");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/connections")
    public ResponseEntity<Map<String, Object>> getConnections() {
        Map<String, Object> response = new HashMap<>();
        response.put("sessions", registry.getSessionIds());
        response.put("count", registry.count());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/broadcast")
    public ResponseEntity<Void> broadcastToAll(@RequestBody Map<String, Object> message) {
        // publica a /topic/sensor (o a la ruta que uses)
        //messagingTemplate.convertAndSend("/topic/sensor", message);
        return ResponseEntity.ok().build();
    }
}
