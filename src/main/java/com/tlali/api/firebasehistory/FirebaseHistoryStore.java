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
import java.util.Set;

@Service
public class FirebaseHistoryStore {

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
			Instant start = candidates.stream().map(candidate -> candidate.response().gatewayReceivedAt())
					.min(Instant::compareTo).orElseThrow();
			Instant end = candidates.stream().map(candidate -> candidate.response().gatewayReceivedAt())
					.max(Instant::compareTo).orElseThrow();
			Set<String> nodes = candidates.stream().map(candidate -> candidate.response().node())
					.collect(java.util.stream.Collectors.toSet());
			repository.findByNodeInAndGatewayReceivedAtBetween(nodes, start, end)
					.forEach(row -> naturalKey(row.getNode(), row.getSequenceNumber(), row.getGatewayReceivedAt())
							.ifPresent(naturalKeys::add));
		}

		for (Candidate candidate : candidates) {
			FirebaseHistoryNodeResponse response = candidate.response();
			String naturalKey = naturalKey(response.node(), response.sequenceNumber(), response.gatewayReceivedAt())
					.orElse(null);
			boolean exactDuplicate = fingerprints.contains(candidate.fingerprint())
					|| (naturalKey != null && naturalKeys.contains(naturalKey));
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
			if (naturalKey != null) {
				naturalKeys.add(naturalKey);
			}
		}

		if (!pending.isEmpty()) {
			repository.saveAllAndFlush(pending);
		}
		boolean allVerified = invalid == 0 && candidates.size() == entries.size();
		return new FirebaseHistoryStoreResult(entries.size(), pending.size(), duplicates, invalid, allVerified);
	}

	private java.util.Optional<String> naturalKey(String node, Long sequenceNumber, Instant gatewayReceivedAt) {
		if (sequenceNumber == null || gatewayReceivedAt == null) {
			return java.util.Optional.empty();
		}
		return java.util.Optional.of(node + "\u0000" + sequenceNumber + "\u0000" + gatewayReceivedAt);
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
