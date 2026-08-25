package com.tlali.api.firebasehistory;

import com.tlali.api.firebase.FirebaseActualResponse;
import com.tlali.api.firebase.FirebaseNodeSnapshot;
import com.tlali.api.firebase.FirebaseRealtimeDatabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class FirebaseHistorySyncService {

	private static final Logger log = LoggerFactory.getLogger(FirebaseHistorySyncService.class);

	private final FirebaseRealtimeDatabaseClient firebaseClient;
	private final boolean enabled;

	public FirebaseHistorySyncService(
			FirebaseRealtimeDatabaseClient firebaseClient,
			@Value("${tlali.firebase.history-sync.enabled:true}") boolean enabled
	) {
		this.firebaseClient = firebaseClient;
		this.enabled = enabled;
	}

	@Scheduled(fixedDelayString = "${tlali.firebase.history-sync.fixed-delay-ms:30000}")
	public void syncActualNodes() {
		if (!enabled) {
			return;
		}

		try {
			FirebaseActualResponse actual = firebaseClient.fetchActual();
			int saved = 0;

			for (FirebaseNodeSnapshot node : actual.nodes().values()) {
				if (node.node() == null || node.node().isBlank()) {
					continue;
				}
				firebaseClient.saveHistorySnapshot(node);
				saved++;
			}

			if (saved > 0) {
				log.info("Firebase history sync wrote {} node snapshots to Realtime Database", saved);
			}
		} catch (RuntimeException exception) {
			log.warn("Firebase history sync skipped: {}", exception.getMessage());
		}
	}
}
