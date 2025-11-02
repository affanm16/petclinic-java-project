package com.example.petclinic.repository;

import com.example.petclinic.model.Owner;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class OwnerRepository {

	private final Map<Long, Owner> owners = new ConcurrentHashMap<>();
	private final AtomicLong sequence = new AtomicLong(1);

	public List<Owner> findAll() {
	return owners.values().stream()
		.sorted(Comparator.comparing(Owner::getId))
		.collect(Collectors.toList());
	}

	public Owner findById(Long id) {
		if (id == null) {
			return null;
		}
		return owners.get(id);
	}

	public Owner save(Owner owner) {
		if (owner.getId() == null) {
			owner.setId(sequence.getAndIncrement());
		}
		owners.put(owner.getId(), owner);
		return owner;
	}
}
