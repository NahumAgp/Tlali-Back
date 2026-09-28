package com.tlali.api.ai;

public record AudioTranscriptionResponse(
		String text,
		String language,
		String model
) {
}
