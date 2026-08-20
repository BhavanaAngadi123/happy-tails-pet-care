package com.happytails.social;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordResetReadinessTest {
 @Autowired MockMvc mvc;

 @Test void statusExplainsWhyResetIsDisabledWithoutMail() throws Exception {
  mvc.perform(get("/api/auth/password-reset/status"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.enabled").value(false))
    .andExpect(jsonPath("$.featureEnabled").value(false))
    .andExpect(jsonPath("$.mailHostConfigured").value(false))
    .andExpect(jsonPath("$.baseUrlConfigured").value(false))
    .andExpect(jsonPath("$.fromAddressConfigured").value(false))
    .andExpect(jsonPath("$.tokenLifetimeMinutes").value(30));
 }
}
