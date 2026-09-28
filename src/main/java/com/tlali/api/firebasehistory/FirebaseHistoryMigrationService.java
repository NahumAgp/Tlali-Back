package com.tlali.api.firebasehistory;

import com.tlali.api.firebase.FirebaseRealtimeDatabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

@Service
public class FirebaseHistoryMigrationService {

	private static final Logger log = LoggerFactory.getLogger(FirebaseHistoryMigrationService.class);
	private static final ZoneId REPORT_ZONE = ZoneId.of("America/Mexico_City");

	private final FirebaseRealtimeDatabaseClient firebaseClient;
	private final FirebaseHistoryStore historyStore;
	private final boolean enabled;
	private final boolean cleanupEnabled;
	private final int lookbackDays;

	public FirebaseHistoryMigrationService(
			FirebaseRealtimeDatabaseClient firebaseClient,
			FirebaseHistoryStore historyStore,
			@Value("${tlali.firebase.history-import.enabled:true}") boolean enabled,
			@Value("${tlali.firebase.cleanup.enabled:false}") boolean cleanupEnabled,
			@Value("${tlali.firebase.history-import.lookback-days:90}") int lookbackDays
	) {
		this.firebaseClient = firebaseClient;
		this.historyStore = historyStore;
		this.enabled = enabled;
		this.cleanupEnabled = cleanupEnabled;
		this.lookbackDays = Math.max(1, lookbackDays);
	}

	@Scheduled(
			initialDelayString = "${tlali.firebase.history-import.initial-delay-ms:15000}",
			fixedDelayString = "${tlali.firebase.history-import.fixed-delay-ms:900000}"
	)
	public void importHistory() {
		if (!enabled) {
			return;
		}

		LocalDate earliestDate = LocalDate.now(REPORT_ZONE).minusDays(lookbackDays - 1L);
		int batches = 0;
		int inserted = 0;
		int duplicates = 0;
		int deleted = 0;
		int failed = 0;

		try {
			Set<String> branches = firebaseClient.fetchHistoryBranches();
			for (String branch : branches) {
				for (LocalDate date : firebaseClient.fetchHistoryDates(branch, earliestDate)) {
					try {
						FirebaseHistoryBatch batch = firebaseClient.fetchHistoryBatch(branch, date);
						if (batch.entries().isEmpty()) {
							continue;
						}
						FirebaseHistoryStoreResult result = historyStore.store(batch.entries());
						batches++;
						inserted += result.inserted();
						duplicates += result.duplicates();
						if (cleanupEnabled && result.allVerified()) {
							firebaseClient.deleteHistoryBatch(branch, date);
							deleted++;
						}
					} catch (RuntimeException exception) {
						failed++;
						log.warn("Firebase history batch {}/{} was kept: {}", branch, date, exception.getMessage());
					}
				}
			}
			log.info(
					"Firebase history import finished: batches={}, inserted={}, duplicates={}, deletedBatches={}, failed={}",
					batches, inserted, duplicates, deleted, failed);
		} catch (RuntimeException exception) {
			log.warn("Firebase history import could not list source data: {}", exception.getMessage());
		}
	}
}
