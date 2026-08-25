package com.tlali.api.firebase;

import com.tlali.api.firebasehistory.FirebaseNodeHistory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Map;

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
	private static final JsonMapper JSON_MAPPER = new JsonMapper();

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

	public static FirebaseHistoryNodeResponse fromFirebase(String nodeName, String type, FirebaseNodeSnapshot snapshot, Instant syncedAt) {
		Instant gatewayReceivedAt = parseGatewayReceivedAt(snapshot.gateway());
		return new FirebaseHistoryNodeResponse(
				null,
				snapshot.node() == null || snapshot.node().isBlank() ? nodeName : snapshot.node(),
				snapshot.type() == null || snapshot.type().isBlank() ? type : snapshot.type(),
				snapshot.seq(),
				toJson(snapshot.data()),
				toJson(snapshot.gateway()),
				toJson(snapshot.radio()),
				toJson(snapshot.valid()),
				gatewayReceivedAt,
				syncedAt
		);
	}

	private static String toJson(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return JSON_MAPPER.writeValueAsString(value);
		} catch (JacksonException exception) {
			return "{}";
		}
	}

	private static Instant parseGatewayReceivedAt(Map<String, Object> gateway) {
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
