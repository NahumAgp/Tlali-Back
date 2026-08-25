package com.tlali.api.firebase;

import com.tlali.api.firebasehistory.FirebaseNodeHistoryRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

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

	@GetMapping("/history")
	public List<FirebaseHistoryNodeResponse> history(
			@RequestParam(defaultValue = "sensor") String type,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
	) {
		LocalDate targetDate = date == null ? LocalDate.now(REPORT_ZONE) : date;
		Instant start = targetDate.atStartOfDay(REPORT_ZONE).toInstant();
		Instant end = targetDate.plusDays(1).atStartOfDay(REPORT_ZONE).toInstant();
		return historyRepository.findTop2000ByTypeAndGatewayReceivedAtBetweenOrderByGatewayReceivedAtDesc(type, start, end)
				.stream()
				.map(FirebaseHistoryNodeResponse::from)
				.toList();
	}
}
