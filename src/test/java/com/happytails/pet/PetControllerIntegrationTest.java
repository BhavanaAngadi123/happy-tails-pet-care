package com.happytails.pet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PetControllerIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void createsAndReadsPet() throws Exception {
        Pet pet = new Pet();
        pet.setName("Milo"); pet.setSpecies("Dog"); pet.setAge(3); pet.setBreed("Beagle");
        mvc.perform(post("/api/pets").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(pet)))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Milo"));
        mvc.perform(get("/api/pets")).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Milo"));
    }
}
