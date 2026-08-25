package com.tlali.api.firebase;

import com.tlali.api.firebasehistory.FirebaseNodeHistory;

import java.time.Instant;

public record FirebaseHistoryNodeResponse(
		Long id,
		String node,
		String type,
		Long sequenceNumber,
		String dataJson,
		String gatewayJson,
		String radioJson,
		String validJson,
		Instant gatewayReceivedAt,
		Instant syncedAt
) {
	public static FirebaseHistoryNodeResponse from(FirebaseNodeHistory history) {
		return new FirebaseHistoryNodeResponse(
				history.getId(),
				history.getNode(),
				history.getType(),
				history.getSequenceNumber(),
				history.getDataJson(),
				history.getGatewayJson(),
				history.getRadioJson(),
				history.getValidJson(),
				history.getGatewayReceivedAt(),
				history.getSyncedAt()
		);
	}
}
