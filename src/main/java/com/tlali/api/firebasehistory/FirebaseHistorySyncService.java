package com.tlali.api.firebasehistory;

import com.tlali.api.firebase.FirebaseActualResponse;
import com.tlali.api.firebase.FirebaseNodeSnapshot;
import com.tlali.api.firebase.FirebaseRealtimeDatabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class FirebaseHistorySyncService {

	private static final Logger log = LoggerFactory.getLogger(FirebaseHistorySyncService.class);

	private final FirebaseRealtimeDatabaseClient firebaseClient;
	private final FirebaseHistoryStore historyStore;
	private final boolean enabled;

	public FirebaseHistorySyncService(
			FirebaseRealtimeDatabaseClient firebaseClient,
			FirebaseHistoryStore historyStore,
			@Value("${tlali.firebase.history-sync.enabled:true}") boolean enabled
	) {
		this.firebaseClient = firebaseClient;
		this.historyStore = historyStore;
		this.enabled = enabled;
	}

	@Scheduled(fixedDelayString = "${tlali.firebase.history-sync.fixed-delay-ms:30000}")
	public void syncActualNodes() {
		if (!enabled) {
			return;
		}

		try {
			FirebaseActualResponse actual = firebaseClient.fetchActual();
			List<FirebaseHistorySourceEntry> entries = new ArrayList<>();

			for (FirebaseNodeSnapshot node : actual.nodes().values()) {
				if (node.node() == null || node.node().isBlank()) {
					continue;
				}
				String receivedAt = Objects.toString(
						node.gateway() == null ? null : node.gateway().get("recibidoUtc"),
						actual.fetchedAt().toString());
				String identity = Objects.toString(node.seq(), "sin-seq") + "-" + receivedAt;
				entries.add(new FirebaseHistorySourceEntry(
						"/tlali/actual/" + node.node() + "/" + identity,
						node.node(),
						node.type(),
						actual.fetchedAt(),
						node
				));
			}

			FirebaseHistoryStoreResult result = historyStore.store(entries);
			if (result.inserted() > 0) {
				log.info("Realtime sync stored {} new node snapshots in MySQL", result.inserted());
			}
		} catch (RuntimeException exception) {
			log.warn("Realtime-to-MySQL sync skipped: {}", exception.getMessage());
		}
	}
}
