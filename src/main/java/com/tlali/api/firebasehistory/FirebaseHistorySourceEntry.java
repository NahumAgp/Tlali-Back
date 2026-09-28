package com.tlali.api.firebasehistory;

import com.tlali.api.firebase.FirebaseNodeSnapshot;

import java.time.Instant;

public record FirebaseHistorySourceEntry(
		String sourcePath,
		String fallbackNode,
		String fallbackType,
		Instant fallbackReceivedAt,
		FirebaseNodeSnapshot snapshot
) {
}
