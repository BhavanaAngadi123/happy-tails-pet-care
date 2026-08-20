package com.happytails.social;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SessionSecurityTest {
  @Autowired MockMvc mvc;

  @Test
  void successfulAuthenticationRotatesSessionIdAndUsesOneDayIdleTimeout() throws Exception {
    MockHttpSession seeded=new MockHttpSession();
    String before=seeded.getId();
    MvcResult result=mvc.perform(post("/api/auth/signup").session(seeded).contentType("application/json")
        .content("{\"email\":\"session-rotate@example.com\",\"password\":\"TestPass123!\",\"displayName\":\"Session Parent\"}"))
      .andExpect(status().isCreated()).andReturn();
    MockHttpSession after=(MockHttpSession)result.getRequest().getSession(false);
    assertNotNull(after);
    assertNotEquals(before,after.getId());
    assertEquals(86400,after.getMaxInactiveInterval());
    assertNotNull(after.getAttribute("authenticatedAt"));
  }

  @Test
  void logoutInvalidatesServerSessionAndExpiresCookie() throws Exception {
    MvcResult signup=mvc.perform(post("/api/auth/signup").contentType("application/json")
        .content("{\"email\":\"session-logout@example.com\",\"password\":\"TestPass123!\",\"displayName\":\"Logout Parent\"}"))
      .andExpect(status().isCreated()).andReturn();
    MockHttpSession session=(MockHttpSession)signup.getRequest().getSession(false);
    mvc.perform(post("/api/auth/logout").session(session))
      .andExpect(status().isOk())
      .andExpect(header().string("Set-Cookie",org.hamcrest.Matchers.containsString("Max-Age=0")));
    assertTrue(session.isInvalid());
  }
}
