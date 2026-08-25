package com.tlali.api.ai;

public class OpenAiNotConfiguredException extends RuntimeException {
	public OpenAiNotConfiguredException() {
		super("OpenAI API key is not configured");
	}
}
