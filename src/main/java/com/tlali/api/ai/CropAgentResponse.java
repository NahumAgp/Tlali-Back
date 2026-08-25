package com.tlali.api.ai;

public record CropAgentResponse(
		String answer,
		String model,
		boolean openAiUsed
) {
}
