package com.tlali.api.firebasehistory;

import com.tlali.api.firebase.FirebaseHistoryNodeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class FirebaseHistoryStore {
	private static final long SAMPLE_INTERVAL_SECONDS = 30 * 60;

	private final FirebaseNodeHistoryRepository repository;

	public FirebaseHistoryStore(FirebaseNodeHistoryRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public FirebaseHistoryStoreResult store(List<FirebaseHistorySourceEntry> entries) {
		List<Candidate> candidates = new ArrayList<>();
		List<FirebaseNodeHistory> pending = new ArrayList<>();
		int duplicates = 0;
		int invalid = 0;

		for (FirebaseHistorySourceEntry entry : entries) {
			FirebaseHistoryNodeResponse response = FirebaseHistoryNodeResponse.fromFirebase(
					entry.fallbackNode(),
					entry.fallbackType(),
					entry.snapshot(),
					Instant.now(),
					entry.fallbackReceivedAt()
			);
			if (response.node() == null || response.node().isBlank() || response.gatewayReceivedAt() == null) {
				invalid++;
				continue;
			}
			candidates.add(new Candidate(response, fingerprint(entry.sourcePath())));
		}

		Set<String> fingerprints = new HashSet<>();
		Set<String> naturalKeys = new HashSet<>();
		if (!candidates.isEmpty()) {
			repository.findAllBySourceFingerprintIn(candidates.stream().map(Candidate::fingerprint).toList())
					.forEach(row -> fingerprints.add(row.getSourceFingerprint()));
			Instant firstReading = candidates.stream().map(candidate -> candidate.response().gatewayReceivedAt())
					.min(Instant::compareTo).orElseThrow();
			Instant lastReading = candidates.stream().map(candidate -> candidate.response().gatewayReceivedAt())
					.max(Instant::compareTo).orElseThrow();
			Instant start = bucketStart(firstReading);
			Instant end = bucketStart(lastReading).plusSeconds(SAMPLE_INTERVAL_SECONDS).minusNanos(1);
			Set<String> nodes = candidates.stream().map(candidate -> candidate.response().node())
					.collect(java.util.stream.Collectors.toSet());
			repository.findByNodeInAndGatewayReceivedAtBetween(nodes, start, end)
					.forEach(row -> naturalKeys.add(sampleKey(
							row.getNode(), row.getType(), row.getGatewayReceivedAt())));
		}

		for (Candidate candidate : candidates) {
			FirebaseHistoryNodeResponse response = candidate.response();
			String naturalKey = sampleKey(response.node(), response.type(), response.gatewayReceivedAt());
			boolean exactDuplicate = fingerprints.contains(candidate.fingerprint())
					|| naturalKeys.contains(naturalKey);
			if (exactDuplicate) {
				duplicates++;
				continue;
			}

			pending.add(new FirebaseNodeHistory(
					candidate.fingerprint(),
					response.node(),
					response.type(),
					response.sequenceNumber(),
					response.dataJson(),
					response.gatewayJson(),
					response.radioJson(),
					response.validJson(),
					response.gatewayReceivedAt(),
					response.syncedAt()
			));
			fingerprints.add(candidate.fingerprint());
			naturalKeys.add(naturalKey);
		}

		if (!pending.isEmpty()) {
			repository.saveAllAndFlush(pending);
		}
		boolean allVerified = invalid == 0 && candidates.size() == entries.size();
		return new FirebaseHistoryStoreResult(entries.size(), pending.size(), duplicates, invalid, allVerified);
	}

	private String sampleKey(String node, String type, Instant gatewayReceivedAt) {
		return node + "\u0000" + Optional.ofNullable(type).orElse("") + "\u0000" + bucketStart(gatewayReceivedAt);
	}

	private Instant bucketStart(Instant instant) {
		long epochSecond = instant.getEpochSecond();
		return Instant.ofEpochSecond(epochSecond - Math.floorMod(epochSecond, SAMPLE_INTERVAL_SECONDS));
	}

	private String fingerprint(String sourcePath) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(sourcePath.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}

	private record Candidate(
			FirebaseHistoryNodeResponse response,
			String fingerprint
	) {
	}
}
