package com.tlali.api.firebase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FirebaseRealtimeDatabaseClient {

	private static final ParameterizedTypeReference<Map<String, FirebaseNodeSnapshot>> NODE_MAP_TYPE =
			new ParameterizedTypeReference<>() {
			};
	private static final ParameterizedTypeReference<Map<String, Map<String, FirebaseNodeSnapshot>>> HISTORY_MAP_TYPE =
			new ParameterizedTypeReference<>() {
			};
	private static final ZoneId REPORT_ZONE = ZoneId.of("America/Mexico_City");

	private final RestClient restClient;
	private final String source;

	public FirebaseRealtimeDatabaseClient(
			@Value("${tlali.firebase.database-url:https://tlali-5edc4-default-rtdb.firebaseio.com}") String databaseUrl
	) {
		this.source = databaseUrl.replaceAll("/+$", "") + "/tlali/actual";
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(5))
				.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(Duration.ofSeconds(8));
		this.restClient = RestClient.builder()
				.baseUrl(databaseUrl.replaceAll("/+$", ""))
				.requestFactory(requestFactory)
				.build();
	}

	public FirebaseActualResponse fetchActual() {
		Map<String, FirebaseNodeSnapshot> nodes = restClient.get()
				.uri("/tlali/actual.json")
				.retrieve()
				.body(NODE_MAP_TYPE);

		return new FirebaseActualResponse(
				source,
				Instant.now(),
				nodes == null ? Map.of() : new LinkedHashMap<>(nodes)
		);
	}

	public List<FirebaseHistoryNodeResponse> fetchHistory(String type, LocalDate startDate, LocalDate endDate) {
		List<FirebaseHistoryNodeResponse> history = new ArrayList<>();
		LocalDate current = startDate;
		while (!current.isAfter(endDate)) {
			Map<String, Map<String, FirebaseNodeSnapshot>> dayNodes = restClient.get()
					.uri("/tlali/historial/{type}/{date}.json", type, current)
					.retrieve()
					.body(HISTORY_MAP_TYPE);
			if (dayNodes != null) {
				dayNodes.forEach((nodeName, nodeEntries) -> {
					if (nodeEntries == null) {
						return;
					}
					nodeEntries.values().forEach((snapshot) -> {
						if (snapshot != null) {
							history.add(FirebaseHistoryNodeResponse.fromFirebase(nodeName, type, snapshot, Instant.now()));
						}
					});
				});
			}
			current = current.plusDays(1);
		}
		return history.stream()
				.sorted(Comparator.comparing(FirebaseHistoryNodeResponse::gatewayReceivedAt, Comparator.nullsLast(Comparator.reverseOrder())))
				.limit(10000)
				.toList();
	}

	public void saveHistorySnapshot(FirebaseNodeSnapshot node) {
		Instant gatewayReceivedAt = parseGatewayReceivedAt(node.gateway());
		Instant effectiveReceivedAt = gatewayReceivedAt == null ? Instant.now() : gatewayReceivedAt;
		LocalDate localDate = effectiveReceivedAt.atZone(REPORT_ZONE).toLocalDate();
		String type = cleanPathSegment(node.type() == null || node.type().isBlank() ? "sensor" : node.type());
		String nodeName = cleanPathSegment(node.node());
		String key = cleanPathSegment(node.seq() == null ? effectiveReceivedAt.toString() : node.seq().toString());
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("node", node.node());
		payload.put("type", node.type());
		payload.put("seq", node.seq());
		payload.put("data", node.data());
		payload.put("gateway", node.gateway());
		payload.put("radio", node.radio());
		payload.put("valid", node.valid());
		payload.put("syncedAt", Instant.now().toString());
		restClient.put()
				.uri("/tlali/historial/{type}/{date}/{node}/{key}.json", type, localDate, nodeName, key)
				.body(payload)
				.retrieve()
				.toBodilessEntity();
	}

	private Instant parseGatewayReceivedAt(Map<String, Object> gateway) {
		Object receivedAt = gateway == null ? null : gateway.get("recibidoUtc");
		if (receivedAt == null) {
			return null;
		}
		try {
			return Instant.parse(receivedAt.toString());
		} catch (RuntimeException exception) {
			return null;
		}
	}

	private String cleanPathSegment(String value) {
		if (value == null || value.isBlank()) {
			return "sin-nodo";
		}
		return value.replaceAll("[.#$\\[\\]/:]", "-");
	}
}
