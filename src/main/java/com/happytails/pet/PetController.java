package com.happytails.pet;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pets")
@CrossOrigin(origins = "*")
public class PetController {
    private final PetService service;

    public PetController(PetService service) { this.service = service; }

    @GetMapping
    public List<Pet> getAll(@RequestParam(required = false) String species) { return service.findAll(species); }

    @GetMapping("/{id}")
    public Pet getById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pet create(@Valid @RequestBody Pet pet) { return service.create(pet); }

    @PutMapping("/{id}")
    public Pet update(@PathVariable Long id, @Valid @RequestBody Pet pet) { return service.update(id, pet); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
