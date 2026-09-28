package com.tlali.api.firebasehistory;

public record FirebaseHistoryStoreResult(
		int total,
		int inserted,
		int duplicates,
		int invalid,
		boolean allVerified
) {
}
