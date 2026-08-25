package com.tlali.api.firebase;

import java.time.Instant;
import java.util.Map;

public record FirebaseActualResponse(
		String source,
		Instant fetchedAt,
		Map<String, FirebaseNodeSnapshot> nodes
) {
}
