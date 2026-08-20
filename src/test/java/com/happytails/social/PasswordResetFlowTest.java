package com.happytails.social;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties={
 "happy-tails.password-reset.enabled=true",
 "happy-tails.password-reset.base-url=http://localhost:8080",
 "happy-tails.password-reset.from=no-reply@example.com",
 "spring.mail.host=localhost"
})
class PasswordResetFlowTest {
 @Autowired MockMvc mvc;
 @MockBean JavaMailSender mailSender;

 @Test void resetTokenIsEmailedSingleUseAndChangesPassword() throws Exception {
  String email="reset-flow@example.com";
  mvc.perform(post("/api/auth/signup").contentType("application/json")
      .content("{\"email\":\""+email+"\",\"password\":\"OldPassword123\",\"displayName\":\"Parent\"}"))
    .andExpect(status().isCreated());

  mvc.perform(post("/api/auth/password-reset/request").contentType("application/json")
      .content("{\"email\":\""+email+"\"}"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.message").value("If an account exists for that email, reset instructions will be sent."));

  ArgumentCaptor<SimpleMailMessage> cap=ArgumentCaptor.forClass(SimpleMailMessage.class);
  verify(mailSender).send(cap.capture());
  String text=cap.getValue().getText();
  Matcher m=Pattern.compile("resetToken=([A-Za-z0-9_-]+)").matcher(text==null?"":text);
  if(!m.find())throw new AssertionError("Reset token missing from email");
  String token=m.group(1);

  mvc.perform(post("/api/auth/password-reset/confirm").contentType("application/json")
      .content("{\"token\":\""+token+"\",\"newPassword\":\"NewPassword456\"}"))
    .andExpect(status().isOk());

  mvc.perform(post("/api/auth/password-reset/confirm").contentType("application/json")
      .content("{\"token\":\""+token+"\",\"newPassword\":\"AnotherPassword789\"}"))
    .andExpect(status().isBadRequest());

  mvc.perform(post("/api/auth/login").contentType("application/json")
      .content("{\"email\":\""+email+"\",\"password\":\"OldPassword123\"}"))
    .andExpect(status().isUnauthorized());
  mvc.perform(post("/api/auth/login").contentType("application/json")
      .content("{\"email\":\""+email+"\",\"password\":\"NewPassword456\"}"))
    .andExpect(status().isOk());
 }
}
