package com.tlali.api.firebase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class FirebaseRealtimeDatabaseClient {

	private static final ParameterizedTypeReference<Map<String, FirebaseNodeSnapshot>> NODE_MAP_TYPE =
			new ParameterizedTypeReference<>() {
			};

	private final RestClient restClient;
	private final String source;

	public FirebaseRealtimeDatabaseClient(
			@Value("${tlali.firebase.database-url:https://tlali-5edc4-default-rtdb.firebaseio.com}") String databaseUrl
	) {
		this.source = databaseUrl.replaceAll("/+$", "") + "/tlali/actual";
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(5))
				.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(Duration.ofSeconds(8));
		this.restClient = RestClient.builder()
				.baseUrl(databaseUrl.replaceAll("/+$", ""))
				.requestFactory(requestFactory)
				.build();
	}

	public FirebaseActualResponse fetchActual() {
		Map<String, FirebaseNodeSnapshot> nodes = restClient.get()
				.uri("/tlali/actual.json")
				.retrieve()
				.body(NODE_MAP_TYPE);

		return new FirebaseActualResponse(
				source,
				Instant.now(),
				nodes == null ? Map.of() : new LinkedHashMap<>(nodes)
		);
	}
}
