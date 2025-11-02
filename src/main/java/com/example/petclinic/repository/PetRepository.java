package com.example.petclinic.repository;

import com.example.petclinic.model.Pet;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class PetRepository {

	private final Map<Long, Pet> pets = new ConcurrentHashMap<>();
	private final AtomicLong sequence = new AtomicLong(1);

	public List<Pet> findAll() {
		return pets.values().stream()
				.sorted(Comparator.comparing(Pet::getId))
				.collect(Collectors.toList());
	}

	public Pet findById(Long id) {
		if (id == null) {
			return null;
		}
		return pets.get(id);
	}

	public Pet save(Pet pet) {
		if (pet.getId() == null) {
			pet.setId(sequence.getAndIncrement());
		}
		pets.put(pet.getId(), pet);
		return pet;
	}
}
