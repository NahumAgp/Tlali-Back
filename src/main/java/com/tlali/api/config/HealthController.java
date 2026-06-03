package com.tlali.api.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

	@GetMapping("/health")
	public Map<String, String> health() {
		return Map.of(
				"status", "ok",
				"service", "tlali-back",
				"timestamp", Instant.now().toString()
		);
	}
}
