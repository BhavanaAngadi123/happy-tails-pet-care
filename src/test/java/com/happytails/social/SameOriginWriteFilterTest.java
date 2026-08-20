package com.happytails.social;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SameOriginWriteFilterTest {
  @Autowired MockMvc mvc;

  @Test
  void crossSiteWriteIsRejectedBeforeController() throws Exception {
    mvc.perform(post("/api/auth/login")
        .header("Origin", "https://evil.example")
        .header("Host", "happytails.example")
        .contentType("application/json")
        .content("{\"email\":\"nobody@example.com\",\"password\":\"wrongpass\"}"))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.error").value("Cross-site write request blocked."));
  }

  @Test
  void sameOriginWriteContinuesToController() throws Exception {
    mvc.perform(post("/api/auth/login")
        .header("Origin", "https://happytails.example")
        .header("Host", "happytails.example")
        .contentType("application/json")
        .content("{\"email\":\"nobody@example.com\",\"password\":\"wrongpass\"}"))
      .andExpect(status().isUnauthorized());
  }
}
