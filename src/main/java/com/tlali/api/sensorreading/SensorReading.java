package com.tlali.api.sensorreading;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
		name = "sensor_readings",
		indexes = {
				@Index(
						name = "idx_sensor_readings_device_received_at",
						columnList = "device_id, received_at"
				)
		}
)
public class SensorReading {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "device_id", nullable = false, length = 80)
	private String deviceId;

	@Column(name = "greenhouse_id", length = 80)
	private String greenhouseId;

	@Column(name = "temperature_celsius", nullable = false, precision = 5, scale = 2)
	private BigDecimal temperatureCelsius;

	@Column(name = "humidity_percent", nullable = false, precision = 5, scale = 2)
	private BigDecimal humidityPercent;

	@Column(name = "soil_moisture_percent", precision = 5, scale = 2)
	private BigDecimal soilMoisturePercent;

	@Column(name = "light_lux", precision = 10, scale = 2)
	private BigDecimal lightLux;

	@Column(name = "battery_voltage", precision = 5, scale = 2)
	private BigDecimal batteryVoltage;

	@Column(name = "recorded_at", nullable = false)
	private Instant recordedAt;

	@Column(name = "received_at", nullable = false)
	private Instant receivedAt;

	protected SensorReading() {
	}

	private SensorReading(
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
		this.deviceId = deviceId;
		this.greenhouseId = greenhouseId;
		this.temperatureCelsius = temperatureCelsius;
		this.humidityPercent = humidityPercent;
		this.soilMoisturePercent = soilMoisturePercent;
		this.lightLux = lightLux;
		this.batteryVoltage = batteryVoltage;
		this.recordedAt = recordedAt;
		this.receivedAt = receivedAt;
	}

	public static SensorReading from(CreateSensorReadingRequest request, Instant receivedAt) {
		Instant recordedAt = request.recordedAt() == null ? receivedAt : request.recordedAt();

		return new SensorReading(
				request.deviceId(),
				request.greenhouseId(),
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

	public String getGreenhouseId() {
		return greenhouseId;
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
