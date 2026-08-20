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
class PasswordSecurityTest {
 @Autowired MockMvc mvc;

 @Test void signupRejectsWeakPassword() throws Exception {
  mvc.perform(post("/api/auth/signup").contentType("application/json").content("{\"email\":\"weak-password@example.com\",\"password\":\"abcdefghij\",\"displayName\":\"Parent\"}"))
    .andExpect(status().isBadRequest());
 }

 @Test void passwordChangeRequiresCurrentPasswordAndChangesCredentials() throws Exception {
  MvcResult signup=mvc.perform(post("/api/auth/signup").contentType("application/json").content("{\"email\":\"change-password@example.com\",\"password\":\"OldPassword123\",\"displayName\":\"Parent\"}"))
    .andExpect(status().isCreated()).andReturn();
  MockHttpSession session=(MockHttpSession)signup.getRequest().getSession(false);
  mvc.perform(post("/api/auth/change-password").session(session).contentType("application/json").content("{\"currentPassword\":\"wrong12345\",\"newPassword\":\"NewPassword456\"}"))
    .andExpect(status().isUnauthorized());
  mvc.perform(post("/api/auth/change-password").session(session).contentType("application/json").content("{\"currentPassword\":\"OldPassword123\",\"newPassword\":\"NewPassword456\"}"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));
  mvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isOk());
  mvc.perform(post("/api/auth/login").contentType("application/json").content("{\"email\":\"change-password@example.com\",\"password\":\"OldPassword123\"}"))
    .andExpect(status().isUnauthorized());
  mvc.perform(post("/api/auth/login").contentType("application/json").content("{\"email\":\"change-password@example.com\",\"password\":\"NewPassword456\"}"))
    .andExpect(status().isOk());
 }

 @Test void duplicateSignupUsesGenericFailureMessage() throws Exception {
  String body="{\"email\":\"duplicate-account@example.com\",\"password\":\"SecurePassword123\",\"displayName\":\"Parent\"}";
  mvc.perform(post("/api/auth/signup").contentType("application/json").content(body)).andExpect(status().isCreated());
  mvc.perform(post("/api/auth/signup").contentType("application/json").content(body))
    .andExpect(status().isConflict()).andExpect(jsonPath("$.error").value("Unable to create account with those details."));
 }
}
