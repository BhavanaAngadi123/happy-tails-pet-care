package com.happytails.social;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OwnerDataExportTest {
 @Autowired MockMvc mvc;

 @Test void exportContainsOwnerAndPetDataButNoCredentialSecrets() throws Exception {
  MvcResult signup=mvc.perform(post("/api/auth/signup").contentType("application/json")
      .content("{\"email\":\"export-owner@example.com\",\"password\":\"SecurePassword123\",\"displayName\":\"Export Parent\"}"))
    .andExpect(status().isCreated()).andReturn();
  MockHttpSession session=(MockHttpSession)signup.getRequest().getSession(false);

  mvc.perform(post("/api/auth/pets").session(session).contentType("application/json")
      .content("{\"name\":\"Panda\",\"handle\":\"export_panda\",\"species\":\"Other\",\"bio\":\"Export me safely\"}"))
    .andExpect(status().isCreated());

  MvcResult export=mvc.perform(get("/api/auth/export").session(session))
    .andExpect(status().isOk())
    .andExpect(header().string("Cache-Control",containsString("no-store")))
    .andExpect(header().string("Content-Disposition",containsString("happy-tails-data.json")))
    .andExpect(jsonPath("$.format").value("happy-tails-owner-export-v1"))
    .andExpect(jsonPath("$.owner.email").value("export-owner@example.com"))
    .andExpect(jsonPath("$.owner.displayName").value("Export Parent"))
    .andExpect(jsonPath("$.pets[0].name").value("Panda"))
    .andReturn();

  String body=export.getResponse().getContentAsString();
  org.junit.jupiter.api.Assertions.assertFalse(body.contains("passwordHash"));
  org.junit.jupiter.api.Assertions.assertFalse(body.contains("password_hash"));
  org.junit.jupiter.api.Assertions.assertFalse(body.contains("tokenHash"));
  org.junit.jupiter.api.Assertions.assertFalse(body.contains("credentialVersion"));
  org.junit.jupiter.api.Assertions.assertFalse(body.contains("owner_credential_versions"));
 }

 @Test void exportRequiresAuthentication() throws Exception {
  mvc.perform(get("/api/auth/export")).andExpect(status().isUnauthorized());
 }
}
