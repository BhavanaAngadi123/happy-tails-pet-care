package com.happytails.pet;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PetService {
    private final PetRepository repository;

    public PetService(PetRepository repository) { this.repository = repository; }

    public List<Pet> findAll(String species) {
        return species == null || species.isBlank() ? repository.findAll() : repository.findBySpeciesIgnoreCase(species);
    }

    public Pet findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pet not found: " + id));
    }

    public Pet create(Pet pet) { return repository.save(pet); }

    public Pet update(Long id, Pet input) {
        Pet pet = findById(id);
        pet.setName(input.getName());
        pet.setSpecies(input.getSpecies());
        pet.setBreed(input.getBreed());
        pet.setAge(input.getAge());
        pet.setOwnerName(input.getOwnerName());
        pet.setNotes(input.getNotes());
        return repository.save(pet);
    }

    public void delete(Long id) { repository.delete(findById(id)); }
}
