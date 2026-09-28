package com.tlali.api.firebase;

import com.tlali.api.firebasehistory.FirebaseHistoryBatch;
import com.tlali.api.firebasehistory.FirebaseHistorySourceEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service
public class FirebaseRealtimeDatabaseClient {

	private static final ParameterizedTypeReference<Map<String, FirebaseNodeSnapshot>> NODE_MAP_TYPE =
			new ParameterizedTypeReference<>() {
			};
	private static final ParameterizedTypeReference<Map<String, Map<String, FirebaseNodeSnapshot>>> HISTORY_MAP_TYPE =
			new ParameterizedTypeReference<>() {
			};
	private static final ParameterizedTypeReference<Map<String, Object>> CONFIGURATION_TYPE =
			new ParameterizedTypeReference<>() {
			};
	private static final ZoneId REPORT_ZONE = ZoneId.of("America/Mexico_City");
	private static final DateTimeFormatter LEGACY_TIME_FORMAT = DateTimeFormatter.ofPattern("HH-mm");
	private static final JsonMapper JSON_MAPPER = new JsonMapper();

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

	public Map<String, Object> fetchConfiguration() {
		Map<String, Object> configuration = restClient.get()
				.uri("/tlali/configuracion.json")
				.retrieve()
				.body(CONFIGURATION_TYPE);
		return configuration == null ? Map.of() : new LinkedHashMap<>(configuration);
	}

	public Map<String, Object> saveConfiguration(Map<String, Object> configuration) {
		Map<String, Object> payload = new LinkedHashMap<>(configuration == null ? Map.of() : configuration);
		payload.put("updatedAt", Instant.now().toString());
		restClient.put()
				.uri("/tlali/configuracion.json")
				.body(payload)
				.retrieve()
				.toBodilessEntity();
		return payload;
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

	public Set<String> fetchHistoryBranches() {
		Map<String, Object> branches = fetchShallow("/tlali/historial.json");
		return new TreeSet<>(branches.keySet());
	}

	public Set<LocalDate> fetchHistoryDates(String branch, LocalDate earliestDate) {
		Map<String, Object> dates = fetchShallow("/tlali/historial/" + cleanPathSegment(branch) + ".json");
		Set<LocalDate> result = new TreeSet<>();
		for (String value : dates.keySet()) {
			try {
				LocalDate date = LocalDate.parse(value);
				if (!date.isBefore(earliestDate)) {
					result.add(date);
				}
			} catch (DateTimeParseException ignored) {
				// Firebase may contain non-date metadata beside the history buckets.
			}
		}
		return result;
	}

	public FirebaseHistoryBatch fetchHistoryBatch(String branch, LocalDate date) {
		Map<String, Object> payload = restClient.get()
				.uri("/tlali/historial/{branch}/{date}.json", branch, date)
				.retrieve()
				.body(CONFIGURATION_TYPE);
		List<FirebaseHistorySourceEntry> entries = new ArrayList<>();
		if (payload != null) {
			flattenHistory(branch, date, payload, new ArrayList<>(), entries);
		}
		return new FirebaseHistoryBatch(branch, date, List.copyOf(entries));
	}

	public void deleteHistoryBatch(String branch, LocalDate date) {
		restClient.delete()
				.uri("/tlali/historial/{branch}/{date}.json", branch, date)
				.retrieve()
				.toBodilessEntity();
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

	private Map<String, Object> fetchShallow(String path) {
		Map<String, Object> response = restClient.get()
				.uri(uriBuilder -> uriBuilder.path(path).queryParam("shallow", "true").build())
				.retrieve()
				.body(CONFIGURATION_TYPE);
		return response == null ? Map.of() : response;
	}

	@SuppressWarnings("unchecked")
	private void flattenHistory(
			String branch,
			LocalDate date,
			Map<String, Object> value,
			List<String> path,
			List<FirebaseHistorySourceEntry> entries
	) {
		if (isSnapshot(value)) {
			FirebaseNodeSnapshot snapshot = JSON_MAPPER.convertValue(value, FirebaseNodeSnapshot.class);
			String fallbackNode = inferNode(branch, path);
			String fallbackType = inferType(branch, snapshot.type());
			Instant fallbackReceivedAt = inferReceivedAt(date, path);
			String sourcePath = "/tlali/historial/" + branch + "/" + date + "/" + String.join("/", path);
			entries.add(new FirebaseHistorySourceEntry(
					sourcePath, fallbackNode, fallbackType, fallbackReceivedAt, snapshot));
			return;
		}

		for (Map.Entry<String, Object> child : value.entrySet()) {
			if (!(child.getValue() instanceof Map<?, ?> childMap)) {
				continue;
			}
			List<String> childPath = new ArrayList<>(path);
			childPath.add(child.getKey());
			flattenHistory(branch, date, (Map<String, Object>) childMap, childPath, entries);
		}
	}

	private boolean isSnapshot(Map<String, Object> value) {
		return value.containsKey("data")
				&& (value.containsKey("node") || value.containsKey("gateway") || value.containsKey("seq"));
	}

	private String inferNode(String branch, List<String> path) {
		for (String segment : path) {
			if (segment.toLowerCase().startsWith("tlali-")) {
				return segment;
			}
		}
		return branch.toLowerCase().startsWith("tlali-") ? branch : "sin-nodo";
	}

	private String inferType(String branch, String snapshotType) {
		if (snapshotType != null && !snapshotType.isBlank()) {
			return snapshotType;
		}
		return branch.toLowerCase().contains("actuador") || branch.equalsIgnoreCase("actuator")
				? "actuator"
				: "sensor";
	}

	private Instant inferReceivedAt(LocalDate date, List<String> path) {
		if (path.isEmpty()) {
			return null;
		}
		try {
			LocalTime time = LocalTime.parse(path.get(path.size() - 1), LEGACY_TIME_FORMAT);
			return LocalDateTime.of(date, time).atZone(REPORT_ZONE).toInstant();
		} catch (DateTimeParseException exception) {
			return null;
		}
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
