package com.example.petclinic.repository;

import com.example.petclinic.model.Visit;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class VisitRepository {

	private final Map<Long, Visit> visits = new ConcurrentHashMap<>();
	private final AtomicLong sequence = new AtomicLong(1);

	public List<Visit> findAll() {
		return visits.values().stream()
				.sorted(Comparator.comparing(Visit::getId))
				.collect(Collectors.toList());
	}

	public Visit save(Visit visit) {
		if (visit.getId() == null) {
			visit.setId(sequence.getAndIncrement());
		}
		visits.put(visit.getId(), visit);
		return visit;
	}
}
