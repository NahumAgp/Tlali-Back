package com.tlali.api.sensorreading;

import java.math.BigDecimal;
import java.time.Instant;

public class SensorReading {

	private Long id;

	private String deviceId;

	private String siteId;

	private BigDecimal temperatureCelsius;

	private BigDecimal humidityPercent;

	private BigDecimal soilMoisturePercent;

	private BigDecimal lightLux;

	private BigDecimal batteryVoltage;

	private Instant recordedAt;

	private Instant receivedAt;

	public SensorReading() {
	}

	private SensorReading(
			Long id,
			String deviceId,
			String siteId,
			BigDecimal temperatureCelsius,
			BigDecimal humidityPercent,
			BigDecimal soilMoisturePercent,
			BigDecimal lightLux,
			BigDecimal batteryVoltage,
			Instant recordedAt,
			Instant receivedAt
	) {
		this.id = id;
		this.deviceId = deviceId;
		this.siteId = siteId;
		this.temperatureCelsius = temperatureCelsius;
		this.humidityPercent = humidityPercent;
		this.soilMoisturePercent = soilMoisturePercent;
		this.lightLux = lightLux;
		this.batteryVoltage = batteryVoltage;
		this.recordedAt = recordedAt;
		this.receivedAt = receivedAt;
	}

	public static SensorReading from(Long id, CreateSensorReadingRequest request, Instant receivedAt) {
		Instant recordedAt = request.recordedAt() == null ? receivedAt : request.recordedAt();

		return new SensorReading(
				id,
				request.deviceId(),
				request.siteId(),
				request.temperatureCelsius(),
				request.humidityPercent(),
				request.soilMoisturePercent(),
				request.lightLux(),
				request.batteryVoltage(),
				recordedAt,
				receivedAt
		);
	}

	public Long getId() {
		return id;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getSiteId() {
		return siteId;
	}

	public BigDecimal getTemperatureCelsius() {
		return temperatureCelsius;
	}

	public BigDecimal getHumidityPercent() {
		return humidityPercent;
	}

	public BigDecimal getSoilMoisturePercent() {
		return soilMoisturePercent;
	}

	public BigDecimal getLightLux() {
		return lightLux;
	}

	public BigDecimal getBatteryVoltage() {
		return batteryVoltage;
	}

	public Instant getRecordedAt() {
		return recordedAt;
	}

	public Instant getReceivedAt() {
		return receivedAt;
	}
}
