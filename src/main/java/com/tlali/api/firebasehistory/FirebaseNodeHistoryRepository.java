package com.tlali.api.firebasehistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface FirebaseNodeHistoryRepository extends JpaRepository<FirebaseNodeHistory, Long> {

	boolean existsBySourceFingerprint(String sourceFingerprint);

	boolean existsByNodeAndSequenceNumberAndGatewayReceivedAt(String node, Long sequenceNumber, Instant gatewayReceivedAt);

	List<FirebaseNodeHistory> findAllBySourceFingerprintIn(Collection<String> sourceFingerprints);

	List<FirebaseNodeHistory> findByNodeInAndGatewayReceivedAtBetween(
			Collection<String> nodes, Instant start, Instant end);

	List<FirebaseNodeHistory> findTop200ByTypeOrderByGatewayReceivedAtDesc(String type);

	List<FirebaseNodeHistory> findTop2000ByTypeAndGatewayReceivedAtBetweenOrderByGatewayReceivedAtDesc(String type, Instant start, Instant end);

	List<FirebaseNodeHistory> findTop10000ByTypeAndGatewayReceivedAtBetweenOrderByGatewayReceivedAtDesc(String type, Instant start, Instant end);

	List<FirebaseNodeHistory> findByTypeAndGatewayReceivedAtBetweenOrderByGatewayReceivedAtDesc(
			String type, Instant start, Instant end, Pageable pageable);

	long countByType(String type);

	List<FirebaseNodeHistory> findByGatewayReceivedAtBefore(Instant cutoff);
}
