package com.happytails.social;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class LoginPageController {
 @GetMapping("/login.html")
 public ResponseEntity<String> login() throws IOException {
  String html=new String(new ClassPathResource("static/login.html").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
  if(!html.contains("/password-reset-ui.js"))html=html.replace("</body>","<script src=\"/password-reset-ui.js\"></script></body>");
  if(!html.contains("/account-deletion-ui.js"))html=html.replace("</body>","<script src=\"/account-deletion-ui.js\"></script></body>");
  return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
 }
}
