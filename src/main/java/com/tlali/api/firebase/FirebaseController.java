package com.tlali.api.firebase;

import com.tlali.api.firebasehistory.FirebaseNodeHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/firebase")
public class FirebaseController {

	private static final ZoneId REPORT_ZONE = ZoneId.of("America/Mexico_City");

	private final FirebaseRealtimeDatabaseClient client;
	private final FirebaseNodeHistoryRepository historyRepository;

	public FirebaseController(FirebaseRealtimeDatabaseClient client, FirebaseNodeHistoryRepository historyRepository) {
		this.client = client;
		this.historyRepository = historyRepository;
	}

	@GetMapping("/actual")
	public FirebaseActualResponse actual() {
		return client.fetchActual();
	}

	@GetMapping("/configuration")
	public Map<String, Object> configuration() {
		return client.fetchConfiguration();
	}

	@PutMapping("/configuration")
	public Map<String, Object> saveConfiguration(@RequestBody Map<String, Object> configuration) {
		return client.saveConfiguration(configuration);
	}

	@GetMapping("/history")
	public List<FirebaseHistoryNodeResponse> history(
			@RequestParam(defaultValue = "sensor") String type,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(defaultValue = "10000") int limit
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
		int safeLimit = Math.max(1, Math.min(limit, 100000));
		return historyRepository.findByTypeAndGatewayReceivedAtBetweenOrderByGatewayReceivedAtDesc(
				type,
				rangeStart.atStartOfDay(REPORT_ZONE).toInstant(),
				rangeEnd.plusDays(1).atStartOfDay(REPORT_ZONE).toInstant(),
				PageRequest.of(0, safeLimit)
		).stream().map(FirebaseHistoryNodeResponse::from).toList();
	}
}
