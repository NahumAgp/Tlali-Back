package com.tlali.api.sensorreading;

import java.time.Instant;
import java.util.List;

public record DashboardSummaryResponse(
		long activeNodes,
		long retainedMessages,
		Integer averageBatteryPercent,
		boolean gatewayOnline,
		long alerts,
		long samples,
		Instant lastReceivedAt,
		SensorReadingResponse latestReading,
		List<SensorReadingResponse> recentReadings
) {
}
