package com.olixc.todo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> getApiInfo() {
        return Map.of(
            "name", "Todo API",
            "version", "1.0.0-SNAPSHOT",
            "description", "A RESTful API for managing todos",
            "status", "UP",
            "documentation", "/swagger-ui.html",
            "health", "/actuator/health",
            "endpoints", Map.of(
                "todos", "/api/v1/todos",
                "health", "/actuator/health",
                "api-docs", "/api-docs"
            )
        );
    }
}
