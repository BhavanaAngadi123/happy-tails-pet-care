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
class OwnerAccountDeletionTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper json;

 @Test void fullAccountDeletionRequiresPasswordAndPhraseAndRemovesAllPets() throws Exception {
  String email="delete-owner-complete@example.com";
  MvcResult signup=mvc.perform(post("/api/auth/signup").contentType("application/json")
      .content("{\"email\":\""+email+"\",\"password\":\"SecurePassword123\",\"displayName\":\"Parent\"}"))
    .andExpect(status().isCreated()).andReturn();
  MockHttpSession session=(MockHttpSession)signup.getRequest().getSession(false);
  long panda=createPet(session,"Panda","owner_delete_panda");
  long coco=createPet(session,"Coco","owner_delete_coco");

  mvc.perform(delete("/api/auth/account").session(session).contentType("application/json")
      .content("{\"password\":\"WrongPassword123\",\"confirmPhrase\":\"DELETE MY ACCOUNT\"}"))
    .andExpect(status().isUnauthorized());

  mvc.perform(delete("/api/auth/account").session(session).contentType("application/json")
      .content("{\"password\":\"SecurePassword123\",\"confirmPhrase\":\"delete my account\"}"))
    .andExpect(status().isBadRequest());

  mvc.perform(get("/api/social/profiles/{id}",panda).session(session)).andExpect(status().isOk());

  mvc.perform(delete("/api/auth/account").session(session).contentType("application/json")
      .content("{\"password\":\"SecurePassword123\",\"confirmPhrase\":\"DELETE MY ACCOUNT\"}"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.deleted").value(true));

  mvc.perform(post("/api/auth/login").contentType("application/json")
      .content("{\"email\":\""+email+"\",\"password\":\"SecurePassword123\"}"))
    .andExpect(status().isUnauthorized());

  mvc.perform(get("/api/social/profiles/{id}",panda)).andExpect(status().isNotFound());
  mvc.perform(get("/api/social/profiles/{id}",coco)).andExpect(status().isNotFound());
 }

 private long createPet(MockHttpSession session,String name,String handle) throws Exception {
  MvcResult result=mvc.perform(post("/api/auth/pets").session(session).contentType("application/json")
      .content("{\"name\":\""+name+"\",\"handle\":\""+handle+"\",\"species\":\"Dog\"}"))
    .andExpect(status().isCreated()).andReturn();
  JsonNode body=json.readTree(result.getResponse().getContentAsString());
  return body.get("id").asLong();
 }
}
