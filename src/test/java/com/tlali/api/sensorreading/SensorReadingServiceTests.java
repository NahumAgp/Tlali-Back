package com.tlali.api.sensorreading;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SensorReadingServiceTests {

	@Autowired
	private SensorReadingService service;

	@BeforeEach
	void setUp() {
		service.clearForTests();
	}

	@Test
	void createsReadingAndReturnsLatest() {
		CreateSensorReadingRequest request = new CreateSensorReadingRequest(
				"esp32-tlali-sensor-01",
				"tlali-tlapixqui-main",
				BigDecimal.valueOf(25.4),
				BigDecimal.valueOf(68.2),
				BigDecimal.valueOf(42.0),
				BigDecimal.valueOf(1230.5),
				BigDecimal.valueOf(3.7),
				Instant.parse("2026-06-03T20:00:00Z")
		);

		SensorReadingResponse created = service.create(request);

		assertThat(created.id()).isNotNull();
		assertThat(created.deviceId()).isEqualTo("esp32-tlali-sensor-01");
		assertThat(service.findLatest(10))
				.singleElement()
				.extracting(SensorReadingResponse::id)
				.isEqualTo(created.id());
	}

	@Test
	void buildsDashboardSummaryFromRecentReadings() {
		service.create(new CreateSensorReadingRequest(
				"esp32-cultivo-01",
				"sector-a1",
				BigDecimal.valueOf(25.4),
				BigDecimal.valueOf(68.2),
				BigDecimal.valueOf(42.0),
				BigDecimal.valueOf(43000),
				BigDecimal.valueOf(4.1),
				Instant.now()
		));

		DashboardSummaryResponse summary = service.getDashboardSummary();

		assertThat(summary.activeNodes()).isEqualTo(1);
		assertThat(summary.gatewayOnline()).isTrue();
		assertThat(summary.averageBatteryPercent()).isEqualTo(90);
		assertThat(summary.alerts()).isZero();
		assertThat(summary.samples()).isEqualTo(1);
		assertThat(summary.recentReadings()).hasSize(1);
	}
}
