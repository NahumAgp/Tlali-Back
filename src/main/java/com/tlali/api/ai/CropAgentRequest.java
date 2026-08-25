package com.tlali.api.ai;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CropAgentRequest(
		@NotBlank String question,
		String date,
		String cropName,
		String stageName,
		Integer readingsCount,
		String firstReadingAt,
		String lastReadingAt,
		Boolean firebaseHistory,
		List<CropMetricContext> metrics
) {
}
