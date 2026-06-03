package com.tlali.api.sensorreading;

import java.math.BigDecimal;
import java.time.Instant;

public record SensorReadingResponse(
		Long id,
		String deviceId,
		String greenhouseId,
		BigDecimal temperatureCelsius,
		BigDecimal humidityPercent,
		BigDecimal soilMoisturePercent,
		BigDecimal lightLux,
		BigDecimal batteryVoltage,
		Instant recordedAt,
		Instant receivedAt
) {
	public static SensorReadingResponse from(SensorReading reading) {
		return new SensorReadingResponse(
				reading.getId(),
				reading.getDeviceId(),
				reading.getGreenhouseId(),
				reading.getTemperatureCelsius(),
				reading.getHumidityPercent(),
				reading.getSoilMoisturePercent(),
				reading.getLightLux(),
				reading.getBatteryVoltage(),
				reading.getRecordedAt(),
				reading.getReceivedAt()
		);
	}
}
