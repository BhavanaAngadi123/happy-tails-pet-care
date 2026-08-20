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
class RequestPayloadLimitTest {
  @Autowired MockMvc mvc;

  @Test
  void oversizedApiPayloadIsRejectedBeforeControllerHandling() throws Exception {
    String huge="{\"caption\":\""+"A".repeat((int)RequestPayloadLimitFilter.MAX_API_BODY_BYTES+1000)+"\"}";
    mvc.perform(post("/api/social/posts").contentType("application/json").content(huge))
      .andExpect(status().isPayloadTooLarge())
      .andExpect(content().contentTypeCompatibleWith("application/json"))
      .andExpect(jsonPath("$.error").value("Request payload is too large. Images must be compressed before upload."));
  }
}
