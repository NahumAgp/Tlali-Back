package com.tlali.api.sensorreading;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SensorReadingService {

	private final SensorReadingRepository repository;

	public SensorReadingService(SensorReadingRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public SensorReadingResponse create(CreateSensorReadingRequest request) {
		SensorReading reading = SensorReading.from(request, Instant.now());
		return SensorReadingResponse.from(repository.save(reading));
	}

	@Transactional(readOnly = true)
	public List<SensorReadingResponse> findLatest(int limit) {
		PageRequest page = PageRequest.of(
				0,
				limit,
				Sort.by(Sort.Direction.DESC, "receivedAt")
		);

		return repository.findAll(page)
				.map(SensorReadingResponse::from)
				.toList();
	}
}
