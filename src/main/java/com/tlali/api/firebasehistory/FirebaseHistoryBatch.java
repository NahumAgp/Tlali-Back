package com.tlali.api.firebasehistory;

import java.time.LocalDate;
import java.util.List;

public record FirebaseHistoryBatch(
		String branch,
		LocalDate date,
		List<FirebaseHistorySourceEntry> entries
) {
}
