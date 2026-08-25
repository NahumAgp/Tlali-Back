package com.tlali.api.firebasehistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface FirebaseNodeHistoryRepository extends JpaRepository<FirebaseNodeHistory, Long> {

	boolean existsByNodeAndSequenceNumber(String node, Long sequenceNumber);

	boolean existsByNodeAndGatewayReceivedAt(String node, Instant gatewayReceivedAt);

	List<FirebaseNodeHistory> findTop200ByTypeOrderByGatewayReceivedAtDesc(String type);

	List<FirebaseNodeHistory> findTop2000ByTypeAndGatewayReceivedAtBetweenOrderByGatewayReceivedAtDesc(String type, Instant start, Instant end);

	List<FirebaseNodeHistory> findTop10000ByTypeAndGatewayReceivedAtBetweenOrderByGatewayReceivedAtDesc(String type, Instant start, Instant end);

	List<FirebaseNodeHistory> findByGatewayReceivedAtBefore(Instant cutoff);
}
