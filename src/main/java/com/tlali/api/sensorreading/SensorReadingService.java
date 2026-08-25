package com.tlali.api.sensorreading;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SensorReadingService {

	private final CopyOnWriteArrayList<SensorReading> readings = new CopyOnWriteArrayList<>();
	private final AtomicLong ids = new AtomicLong(1);

	public SensorReadingResponse create(CreateSensorReadingRequest request) {
		SensorReading reading = SensorReading.from(ids.getAndIncrement(), request, Instant.now());
		readings.add(reading);
		return SensorReadingResponse.from(reading);
	}

	public List<SensorReadingResponse> findLatest(int limit) {
		return readings.stream()
				.sorted(Comparator.comparing(SensorReading::getReceivedAt).reversed())
				.limit(Math.max(1, limit))
				.map(SensorReadingResponse::from)
				.toList();
	}

	public DashboardSummaryResponse getDashboardSummary() {
		List<SensorReadingResponse> recentReadings = findLatest(24);
		Instant now = Instant.now();
		Instant activeSince = now.minus(15, ChronoUnit.MINUTES);

		long activeNodes = recentReadings.stream()
				.filter(reading -> !reading.receivedAt().isBefore(activeSince))
				.map(SensorReadingResponse::deviceId)
				.distinct()
				.count();

		List<Double> batteryPercentages = recentReadings.stream()
				.map(SensorReadingResponse::batteryVoltage)
				.filter(Objects::nonNull)
				.map(voltage -> Math.max(0, Math.min(100, (voltage.doubleValue() - 3.2) / 1.0 * 100)))
				.toList();

		Integer averageBatteryPercent = batteryPercentages.isEmpty()
				? null
				: (int) Math.round(batteryPercentages.stream().mapToDouble(Double::doubleValue).average().orElse(0));

		long alerts = recentReadings.stream()
				.filter(this::isOutsideHealthyRange)
				.count();

		SensorReadingResponse latest = recentReadings.isEmpty() ? null : recentReadings.get(0);
		Instant lastReceivedAt = latest == null ? null : latest.receivedAt();
		boolean gatewayOnline = lastReceivedAt != null && !lastReceivedAt.isBefore(activeSince);

		return new DashboardSummaryResponse(
				activeNodes,
				0,
				averageBatteryPercent,
				gatewayOnline,
				alerts,
				readings.size(),
				lastReceivedAt,
				latest,
				recentReadings
		);
	}

	void clearForTests() {
		readings.clear();
		ids.set(1);
	}

	private boolean isOutsideHealthyRange(SensorReadingResponse reading) {
		return reading.temperatureCelsius().doubleValue() < 18
				|| reading.temperatureCelsius().doubleValue() > 30
				|| reading.humidityPercent().doubleValue() < 45
				|| reading.humidityPercent().doubleValue() > 80
				|| reading.soilMoisturePercent() != null
				&& (reading.soilMoisturePercent().doubleValue() < 35
				|| reading.soilMoisturePercent().doubleValue() > 70);
	}
}
