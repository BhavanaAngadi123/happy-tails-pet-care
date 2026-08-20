package com.happytails.social;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PetDeletionAuthorizationTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper json;

 @Test void deletingActivePetSelectsAnotherOwnedPet() throws Exception {
  MockHttpSession session=signup("delete-switch@example.com");
  long panda=createPet(session,"Panda","delete_panda");
  long coco=createPet(session,"Coco","delete_coco");
  mvc.perform(post("/api/auth/select-pet/{id}",panda).session(session)).andExpect(status().isOk());
  mvc.perform(delete("/api/auth/pets/{id}",panda).session(session).contentType("application/json").content("{\"confirmName\":\"Panda\"}"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.deleted").value(true)).andExpect(jsonPath("$.nextPetId").value(coco)).andExpect(jsonPath("$.hasPets").value(true));
  mvc.perform(get("/api/auth/session").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.activePetId").value(coco));
 }

 @Test void ownerCannotDeleteAnotherOwnersPet() throws Exception {
  MockHttpSession first=signup("delete-owner-a@example.com");
  MockHttpSession second=signup("delete-owner-b@example.com");
  long чужой=createPet(second,"Luna","delete_luna_other");
  mvc.perform(delete("/api/auth/pets/{id}",чужой).session(first).contentType("application/json").content("{\"confirmName\":\"Luna\"}"))
    .andExpect(status().isForbidden());
 }

 @Test void deletionRequiresExactPetNameConfirmation() throws Exception {
  MockHttpSession session=signup("delete-confirm@example.com");
  long pet=createPet(session,"Milo","delete_milo_confirm");
  mvc.perform(delete("/api/auth/pets/{id}",pet).session(session).contentType("application/json").content("{\"confirmName\":\"Wrong\"}"))
    .andExpect(status().isBadRequest());
  mvc.perform(get("/api/social/profiles/{id}",pet).session(session)).andExpect(status().isOk());
 }

 private MockHttpSession signup(String email) throws Exception {
  MvcResult result=mvc.perform(post("/api/auth/signup").contentType("application/json").content("{\"email\":\""+email+"\",\"password\":\"SecurePassword123\",\"displayName\":\"Parent\"}"))
    .andExpect(status().isCreated()).andReturn();
  return (MockHttpSession)result.getRequest().getSession(false);
 }
 private long createPet(MockHttpSession session,String name,String handle) throws Exception {
  MvcResult result=mvc.perform(post("/api/auth/pets").session(session).contentType("application/json").content("{\"name\":\""+name+"\",\"handle\":\""+handle+"\",\"species\":\"Dog\"}"))
    .andExpect(status().isCreated()).andReturn();
  JsonNode body=json.readTree(result.getResponse().getContentAsString());return body.get("id").asLong();
 }
}
