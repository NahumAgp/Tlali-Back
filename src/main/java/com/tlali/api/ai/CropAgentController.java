package com.tlali.api.ai;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class CropAgentController {

	private final OpenAiCropAgentService service;

	public CropAgentController(OpenAiCropAgentService service) {
		this.service = service;
	}

	@PostMapping("/crop-agent")
	public CropAgentResponse ask(@Valid @RequestBody CropAgentRequest request) {
		return service.ask(request);
	}

	@ExceptionHandler(OpenAiNotConfiguredException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public Map<String, String> openAiNotConfigured(OpenAiNotConfiguredException exception) {
		return Map.of(
				"message", "Falta configurar OPENAI_API_KEY en el backend para usar el agente de OpenAI.",
				"detail", exception.getMessage()
		);
	}

	@ExceptionHandler(RestClientResponseException.class)
	@ResponseStatus(HttpStatus.BAD_GATEWAY)
	public Map<String, String> openAiRequestFailed(RestClientResponseException exception) {
		String body = exception.getResponseBodyAsString();
		String message = body.contains("credit_balance_exhausted")
				? "La llave de OpenAI fue reconocida, pero la cuenta no tiene créditos disponibles."
				: "OpenAI respondió con un error. Revisa la llave, el modelo configurado o la facturación.";
		return Map.of(
				"message", message,
				"detail", "OpenAI HTTP " + exception.getStatusCode().value()
		);
	}
}
