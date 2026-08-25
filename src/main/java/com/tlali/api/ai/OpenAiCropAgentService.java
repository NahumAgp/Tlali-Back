package com.tlali.api.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiCropAgentService {

	private final RestClient restClient;
	private final String apiKey;
	private final String model;

	public OpenAiCropAgentService(
			@Value("${tlali.openai.api-key:}") String apiKey,
			@Value("${tlali.openai.model:gpt-5}") String model
	) {
		this.apiKey = apiKey;
		this.model = model;
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(Duration.ofSeconds(45));
		this.restClient = RestClient.builder()
				.baseUrl("https://api.openai.com/v1")
				.requestFactory(requestFactory)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}

	public CropAgentResponse ask(CropAgentRequest request) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new OpenAiNotConfiguredException();
		}

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("model", model);
		payload.put("instructions", """
				Eres el agente agricola de Tlali Tlapixqui para un cultivo de jitomate en invernadero.
				Responde en espanol claro, breve y accionable.
				Usa solamente los datos entregados en el contexto. Si faltan datos, dilo sin inventar.
				Compara contra los rangos configurados y prioriza riesgos de cultivo: humedad, temperatura, pH, CE y NPK.
				No des diagnosticos definitivos de enfermedad; recomienda revisar o confirmar cuando aplique.
				""");
		payload.put("input", buildInput(request));

		Map<?, ?> response = restClient.post()
				.uri("/responses")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.body(payload)
				.retrieve()
				.body(Map.class);

		return new CropAgentResponse(extractText(response), model, true);
	}

	private List<Map<String, Object>> buildInput(CropAgentRequest request) {
		List<Map<String, Object>> input = new ArrayList<>();
		Map<String, Object> message = new LinkedHashMap<>();
		message.put("role", "user");
		message.put("content", """
				Pregunta del usuario:
				%s

				Contexto del cultivo:
				Fecha: %s
				Cultivo: %s
				Etapa: %s
				Fuente: %s
				Registros analizados: %s
				Primer registro: %s
				Ultimo registro: %s

				Metricas resumidas:
				%s
				""".formatted(
				request.question(),
				valueOrDash(request.date()),
				valueOrDash(request.cropName()),
				valueOrDash(request.stageName()),
				Boolean.TRUE.equals(request.firebaseHistory()) ? "Firebase historico" : "lecturas disponibles en la sesion",
				request.readingsCount() == null ? 0 : request.readingsCount(),
				valueOrDash(request.firstReadingAt()),
				valueOrDash(request.lastReadingAt()),
				formatMetrics(request.metrics())
		));
		input.add(message);
		return input;
	}

	private String formatMetrics(List<CropMetricContext> metrics) {
		if (metrics == null || metrics.isEmpty()) {
			return "Sin metricas disponibles.";
		}
		List<String> lines = new ArrayList<>();
		for (CropMetricContext metric : metrics) {
			lines.add("- %s: promedio %s%s, minimo %s%s, maximo %s%s, rango %s%s a %s%s, fuera de rango %s%%".formatted(
					valueOrDash(metric.label()),
					numberOrDash(metric.average()),
					valueOrEmpty(metric.unit()),
					numberOrDash(metric.minimum()),
					valueOrEmpty(metric.unit()),
					numberOrDash(metric.maximum()),
					valueOrEmpty(metric.unit()),
					numberOrDash(metric.rangeMin()),
					valueOrEmpty(metric.unit()),
					numberOrDash(metric.rangeMax()),
					valueOrEmpty(metric.unit()),
					metric.outOfRangePercent() == null ? 0 : metric.outOfRangePercent()
			));
		}
		return String.join("\n", lines);
	}

	private String extractText(Map<?, ?> response) {
		Object outputText = response == null ? null : response.get("output_text");
		if (outputText != null && !outputText.toString().isBlank()) {
			return outputText.toString();
		}
		Object output = response == null ? null : response.get("output");
		if (output instanceof List<?> outputItems) {
			StringBuilder builder = new StringBuilder();
			for (Object outputItem : outputItems) {
				if (!(outputItem instanceof Map<?, ?> outputMap)) {
					continue;
				}
				Object content = outputMap.get("content");
				if (!(content instanceof List<?> contentItems)) {
					continue;
				}
				for (Object contentItem : contentItems) {
					if (contentItem instanceof Map<?, ?> contentMap) {
						Object text = contentMap.get("text");
						if (text != null) {
							builder.append(text).append("\n");
						}
					}
				}
			}
			if (!builder.isEmpty()) {
				return builder.toString().trim();
			}
		}
		return "OpenAI respondio, pero no pude interpretar el texto de salida.";
	}

	private String valueOrDash(String value) {
		return value == null || value.isBlank() ? "-" : value;
	}

	private String valueOrEmpty(String value) {
		return value == null ? "" : value;
	}

	private String numberOrDash(Double value) {
		if (value == null) return "-";
		return String.format("%.2f", value);
	}
}
