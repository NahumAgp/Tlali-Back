package com.tlali.api.sensorreading;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateSensorReadingRequest(
		@NotBlank
		@Size(max = 80)
		String deviceId,

		@Size(max = 80)
		String siteId,

		@NotNull
		@DecimalMin("-40.0")
		@DecimalMax("85.0")
		BigDecimal temperatureCelsius,

		@NotNull
		@DecimalMin("0.0")
		@DecimalMax("100.0")
		BigDecimal humidityPercent,

		@DecimalMin("0.0")
		@DecimalMax("100.0")
		BigDecimal soilMoisturePercent,

		@DecimalMin("0.0")
		BigDecimal lightLux,

		@DecimalMin("0.0")
		@DecimalMax("6.0")
		BigDecimal batteryVoltage,

		Instant recordedAt
) {
}
