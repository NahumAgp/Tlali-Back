package com.tlali.api.firebasehistory;

import com.tlali.api.firebase.FirebaseNodeSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FirebaseHistoryStoreTests {

	@Autowired
	private FirebaseHistoryStore store;

	@Autowired
	private FirebaseNodeHistoryRepository repository;

	@BeforeEach
	void cleanDatabase() {
		repository.deleteAll();
	}

	@Test
	void storesAndVerifiesHistoricalSnapshot() {
		FirebaseHistorySourceEntry entry = entry("/tlali/historial/sensor/2026-09-28/tlali-npk-01/42");

		FirebaseHistoryStoreResult result = store.store(List.of(entry));

		assertThat(result.inserted()).isEqualTo(1);
		assertThat(result.invalid()).isZero();
		assertThat(result.allVerified()).isTrue();
		assertThat(repository.count()).isEqualTo(1);
		assertThat(repository.findAll().get(0).getNode()).isEqualTo("tlali-npk-01");
	}

	@Test
	void doesNotDuplicateSameSourceOrEquivalentReading() {
		FirebaseHistorySourceEntry first = entry("/tlali/historial/sensor/2026-09-28/tlali-npk-01/42");
		FirebaseHistorySourceEntry sameReadingOtherPath = entry("/tlali/historial/tlali-npk-01/2026-09-28/12-30");

		store.store(List.of(first));
		FirebaseHistoryStoreResult result = store.store(List.of(first, sameReadingOtherPath));

		assertThat(result.inserted()).isZero();
		assertThat(result.duplicates()).isEqualTo(2);
		assertThat(result.allVerified()).isTrue();
		assertThat(repository.count()).isEqualTo(1);
	}

	private FirebaseHistorySourceEntry entry(String sourcePath) {
		Instant receivedAt = Instant.parse("2026-09-28T18:30:00Z");
		FirebaseNodeSnapshot snapshot = new FirebaseNodeSnapshot(
				"tlali-npk-01",
				"sensor",
				42L,
				Map.of("ph", 5.8, "airTemperatureC", 24.2),
				Map.of("recibidoUtc", receivedAt.toString()),
				Map.of("rssi", -70),
				Map.of("data", true)
		);
		return new FirebaseHistorySourceEntry(
				sourcePath, "tlali-npk-01", "sensor", receivedAt, snapshot);
	}
}
