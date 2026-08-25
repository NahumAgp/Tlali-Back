package com.tlali.api.ai;

public record CropMetricContext(
		String label,
		Double average,
		Double minimum,
		Double maximum,
		Double rangeMin,
		Double rangeMax,
		Integer outOfRangePercent,
		String unit
) {
}
