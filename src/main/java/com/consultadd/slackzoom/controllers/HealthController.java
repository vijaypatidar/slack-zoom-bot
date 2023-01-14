package com.consultadd.slackzoom.controllers;

import java.util.Date;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    Map<String, Object> health() {
        return Map.of(
                "server_time", new Date(),
                "health_check", "passed"
        );
    }

}
