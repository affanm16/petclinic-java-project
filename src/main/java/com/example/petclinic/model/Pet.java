package com.example.petclinic.model;

import java.util.ArrayList;
import java.util.List;

public class Pet {
	private Long id;
	private String name;
	private String type;
	private int age;
	private Owner owner;
	private List<Visit> visits = new ArrayList<>();

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Owner getOwner() {
		return owner;
	}

	public void setOwner(Owner owner) {
		this.owner = owner;
	}

	public List<Visit> getVisits() {
		return visits;
	}

	public void setVisits(List<Visit> visits) {
		this.visits = visits;
	}

	public void addVisit(Visit visit) {
		this.visits.removeIf(existing -> existing.getId() != null && existing.getId().equals(visit.getId()));
		this.visits.add(visit);
	}

    // Getters and Setters
}
