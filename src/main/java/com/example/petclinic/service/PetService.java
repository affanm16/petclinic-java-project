package com.example.petclinic.service;

import com.example.petclinic.model.Owner;
import com.example.petclinic.model.Pet;
import com.example.petclinic.repository.OwnerRepository;
import com.example.petclinic.repository.PetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetService {
    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;

    public PetService(PetRepository petRepository, OwnerRepository ownerRepository) {
        this.petRepository = petRepository;
        this.ownerRepository = ownerRepository;
    }

    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    public void savePet(Pet pet) {
        Owner owner = null;
        if (pet.getOwner() != null && pet.getOwner().getId() != null) {
            owner = ownerRepository.findById(pet.getOwner().getId());
        }

        Pet saved = petRepository.save(pet);

        if (owner != null) {
            saved.setOwner(owner);
            owner.addPet(saved);
        } else {
            saved.setOwner(null);
        }
    }

    public Pet getPet(Long id) {
        return petRepository.findById(id);
    }
}
