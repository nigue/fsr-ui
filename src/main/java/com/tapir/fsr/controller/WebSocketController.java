package com.tapir.fsr.controller;

import com.tapir.fsr.handler.FsrWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/websocket")
public class WebSocketController {

    @Autowired
    private FsrWebSocketHandler webSocketHandler;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getWebSocketStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("activeConnections", webSocketHandler.getActiveSessionCount());
        response.put("status", "running");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/broadcast")
    public ResponseEntity<Void> broadcastToAll(@RequestBody Map<String, Object> message) {
        webSocketHandler.broadcast(message);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/connections")
    public ResponseEntity<Map<String, Object>> getConnections() {
        Map<String, Object> response = new HashMap<>();
        response.put("sessions", webSocketHandler.getActiveSessionIds());
        response.put("count", webSocketHandler.getActiveSessionCount());
        return ResponseEntity.ok(response);
    }
}