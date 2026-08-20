package com.happytails.social;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AbuseRateLimitFilterTest {
  @Autowired MockMvc mvc;

  @Test
  void repeatedLoginAttemptsAreThrottled() throws Exception {
    for(int i=0;i<10;i++){
      mvc.perform(post("/api/auth/login")
          .header("X-Forwarded-For","203.0.113.44")
          .contentType("application/json")
          .content("{\"email\":\"nobody@example.com\",\"password\":\"wrongpass\"}"))
        .andExpect(status().isUnauthorized());
    }
    mvc.perform(post("/api/auth/login")
        .header("X-Forwarded-For","203.0.113.44")
        .contentType("application/json")
        .content("{\"email\":\"nobody@example.com\",\"password\":\"wrongpass\"}"))
      .andExpect(status().isTooManyRequests())
      .andExpect(header().string("Retry-After","60"));
  }

  @Test
  void normalReadRequestsAreNotRateLimited() throws Exception {
    for(int i=0;i<75;i++){
      mvc.perform(get("/api/social/health").header("X-Forwarded-For","203.0.113.45"))
        .andExpect(status().isOk());
    }
  }
}
