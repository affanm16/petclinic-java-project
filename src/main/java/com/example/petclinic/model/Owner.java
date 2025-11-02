package com.example.petclinic.model;

import java.util.ArrayList;
import java.util.List;

public class Owner {
	private Long id;
	private String name;
	private String contactNumber;
	private List<Pet> pets = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public List<Pet> getPets() {
		return pets;
	}

	public void setPets(List<Pet> pets) {
		this.pets = pets;
	}

	public void addPet(Pet pet) {
		this.pets.removeIf(existing -> existing.getId() != null && existing.getId().equals(pet.getId()));
		this.pets.add(pet);
	}

    // Getters and Setters
}
