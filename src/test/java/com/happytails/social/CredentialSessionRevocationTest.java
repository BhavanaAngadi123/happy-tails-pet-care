package com.happytails.social;

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
class CredentialSessionRevocationTest {
 @Autowired MockMvc mvc;

 @Test void changingPasswordRevokesOtherExistingSessions() throws Exception {
  String email="session-revoke@example.com";
  MvcResult signup=mvc.perform(post("/api/auth/signup").header("X-Forwarded-For","203.0.113.81").contentType("application/json")
      .content("{\"email\":\""+email+"\",\"password\":\"OriginalPass123\",\"displayName\":\"Parent\"}"))
    .andExpect(status().isCreated()).andReturn();
  MockHttpSession first=(MockHttpSession)signup.getRequest().getSession(false);

  MvcResult login=mvc.perform(post("/api/auth/login").header("X-Forwarded-For","203.0.113.82").contentType("application/json")
      .content("{\"email\":\""+email+"\",\"password\":\"OriginalPass123\"}"))
    .andExpect(status().isOk()).andReturn();
  MockHttpSession second=(MockHttpSession)login.getRequest().getSession(false);

  mvc.perform(get("/api/auth/session").session(first)).andExpect(status().isOk());
  mvc.perform(get("/api/auth/session").session(second)).andExpect(status().isOk());

  mvc.perform(post("/api/auth/change-password").session(first).contentType("application/json")
      .content("{\"currentPassword\":\"OriginalPass123\",\"newPassword\":\"ChangedPass456\"}"))
    .andExpect(status().isOk());

  mvc.perform(get("/api/auth/session").session(first)).andExpect(status().isOk());
  mvc.perform(get("/api/auth/session").session(second))
    .andExpect(status().isUnauthorized())
    .andExpect(jsonPath("$.error").value("Your session expired because account credentials changed. Please log in again."));
 }
}
