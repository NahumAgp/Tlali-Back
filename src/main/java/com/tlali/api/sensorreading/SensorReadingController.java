package com.tlali.api.sensorreading;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/sensor-readings")
public class SensorReadingController {

	private final SensorReadingService service;

	public SensorReadingController(SensorReadingService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<SensorReadingResponse> create(
			@Valid @RequestBody CreateSensorReadingRequest request
	) {
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(service.create(request));
	}

	@GetMapping("/latest")
	public List<SensorReadingResponse> latest(
			@RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit
	) {
		return service.findLatest(limit);
	}
}
