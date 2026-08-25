package com.tlali.api.firebase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FirebaseNodeSnapshot(
		String node,
		String type,
		Long seq,
		Map<String, Object> data,
		Map<String, Object> gateway,
		Map<String, Object> radio,
		Map<String, Object> valid
) {
}
