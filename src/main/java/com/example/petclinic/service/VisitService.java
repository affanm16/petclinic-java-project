package com.example.petclinic.service;

import com.example.petclinic.model.Visit;
import com.example.petclinic.model.Pet;
import com.example.petclinic.repository.PetRepository;
import com.example.petclinic.repository.VisitRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisitService {
    private final VisitRepository visitRepository;
    private final PetRepository petRepository;

    public VisitService(VisitRepository visitRepository, PetRepository petRepository) {
        this.visitRepository = visitRepository;
        this.petRepository = petRepository;
    }

    public List<Visit> getAllVisits() {
        return visitRepository.findAll();
    }

    public void saveVisit(Visit visit) {
        Pet pet = null;
        if (visit.getPet() != null && visit.getPet().getId() != null) {
            pet = petRepository.findById(visit.getPet().getId());
        }

        Visit saved = visitRepository.save(visit);

        if (pet != null) {
            saved.setPet(pet);
            pet.addVisit(saved);
        } else {
            saved.setPet(null);
        }
    }
}
