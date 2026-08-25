package com.tlali.api.firebasehistory;

import com.tlali.api.firebase.FirebaseActualResponse;
import com.tlali.api.firebase.FirebaseNodeSnapshot;
import com.tlali.api.firebase.FirebaseRealtimeDatabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Map;

@Service
public class FirebaseHistorySyncService {

	private static final Logger log = LoggerFactory.getLogger(FirebaseHistorySyncService.class);

	private final FirebaseRealtimeDatabaseClient firebaseClient;
	private final FirebaseNodeHistoryRepository repository;
	private final JsonMapper jsonMapper;
	private final boolean enabled;
	private final boolean cleanupEnabled;
	private final int retentionDays;

	public FirebaseHistorySyncService(
			FirebaseRealtimeDatabaseClient firebaseClient,
			FirebaseNodeHistoryRepository repository,
			JsonMapper jsonMapper,
			@Value("${tlali.firebase.history-sync.enabled:true}") boolean enabled,
			@Value("${tlali.firebase.cleanup.enabled:false}") boolean cleanupEnabled,
			@Value("${tlali.firebase.cleanup.retention-days:2}") int retentionDays
	) {
		this.firebaseClient = firebaseClient;
		this.repository = repository;
		this.jsonMapper = jsonMapper;
		this.enabled = enabled;
		this.cleanupEnabled = cleanupEnabled;
		this.retentionDays = retentionDays;
	}

	@Scheduled(fixedDelayString = "${tlali.firebase.history-sync.fixed-delay-ms:30000}")
	@Transactional
	public void syncActualNodes() {
		if (!enabled) {
			return;
		}

		try {
			FirebaseActualResponse actual = firebaseClient.fetchActual();
			Instant syncedAt = Instant.now();
			int saved = 0;

			for (FirebaseNodeSnapshot node : actual.nodes().values()) {
				if (node.node() == null || node.node().isBlank()) {
					continue;
				}
				Instant gatewayReceivedAt = parseGatewayReceivedAt(node.gateway());
				if (alreadySynced(node, gatewayReceivedAt)) {
					continue;
				}
				repository.save(toHistory(node, gatewayReceivedAt, syncedAt));
				saved++;
			}

			if (saved > 0) {
				log.info("Firebase history sync saved {} node snapshots", saved);
			}

			if (cleanupEnabled) {
				log.info("Firebase cleanup is enabled with {} day retention, but deletion is intentionally not executed until the Firebase history path is configured.", retentionDays);
			}
		} catch (RuntimeException exception) {
			log.warn("Firebase history sync skipped: {}", exception.getMessage());
		}
	}

	private boolean alreadySynced(FirebaseNodeSnapshot node, Instant gatewayReceivedAt) {
		if (node.seq() != null) {
			return repository.existsByNodeAndSequenceNumber(node.node(), node.seq());
		}
		return gatewayReceivedAt != null && repository.existsByNodeAndGatewayReceivedAt(node.node(), gatewayReceivedAt);
	}

	private FirebaseNodeHistory toHistory(FirebaseNodeSnapshot node, Instant gatewayReceivedAt, Instant syncedAt) {
		return new FirebaseNodeHistory(
				node.node(),
				node.type(),
				node.seq(),
				toJson(node.data()),
				toJson(node.gateway()),
				toJson(node.radio()),
				toJson(node.valid()),
				gatewayReceivedAt,
				syncedAt
		);
	}

	private String toJson(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return jsonMapper.writeValueAsString(value);
		} catch (JacksonException exception) {
			return "{}";
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
}
