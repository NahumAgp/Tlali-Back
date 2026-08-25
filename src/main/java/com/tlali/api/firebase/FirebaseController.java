package com.tlali.api.firebase;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/v1/firebase")
public class FirebaseController {

	private static final ZoneId REPORT_ZONE = ZoneId.of("America/Mexico_City");

	private final FirebaseRealtimeDatabaseClient client;

	public FirebaseController(FirebaseRealtimeDatabaseClient client) {
		this.client = client;
	}

	@GetMapping("/actual")
	public FirebaseActualResponse actual() {
		return client.fetchActual();
	}

	@GetMapping("/history")
	public List<FirebaseHistoryNodeResponse> history(
			@RequestParam(defaultValue = "sensor") String type,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
	) {
		LocalDate rangeStart = startDate != null ? startDate : date;
		LocalDate rangeEnd = endDate != null ? endDate : date;
		LocalDate today = LocalDate.now(REPORT_ZONE);
		if (rangeStart == null) {
			rangeStart = today;
		}
		if (rangeEnd == null || rangeEnd.isBefore(rangeStart)) {
			rangeEnd = rangeStart;
		}
		return client.fetchHistory(type, rangeStart, rangeEnd);
	}
}
