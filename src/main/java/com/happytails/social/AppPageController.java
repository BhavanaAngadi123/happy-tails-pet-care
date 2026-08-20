package com.happytails.social;

import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class AppPageController {
  @GetMapping({"/","/app"})
  public ResponseEntity<?> app(HttpSession session) throws IOException {
    Object owner=session.getAttribute("ownerId");
    if(owner==null)return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION,"/login.html").build();
    Object active=session.getAttribute("activePetId");
    if(!(active instanceof Long))return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION,"/login.html?manage=1").build();
    String html=new String(new ClassPathResource("static/index.html").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    html=html.replace("const API='/api/social';let me=1,","const API='/api/social';let me="+active+",");
    html=html.replace("<div class=\"top-actions\">","<div class=\"top-actions\"><button class=\"icon-btn\" title=\"Switch pet\" onclick=\"location.href='/login.html?manage=1'\">🐾</button><button class=\"icon-btn\" title=\"Log out\" onclick=\"fetch('/api/auth/logout',{method:'POST'}).then(()=>location.href='/login.html')\">↪</button>");
    html=html.replace("</head>","<link rel=\"stylesheet\" href=\"/product-ux.css\"><link rel=\"stylesheet\" href=\"/messaging.css\"><link rel=\"stylesheet\" href=\"/playdate-flow.css\"><link rel=\"stylesheet\" href=\"/health-passport.css\"><link rel=\"stylesheet\" href=\"/memories-milestones.css\"><link rel=\"stylesheet\" href=\"/communities.css\"><link rel=\"stylesheet\" href=\"/smart-discovery.css\"><link rel=\"stylesheet\" href=\"/trust-safety.css\"><link rel=\"stylesheet\" href=\"/qa-polish.css\"><link rel=\"stylesheet\" href=\"/journey-flow.css\"><link rel=\"stylesheet\" href=\"/visual-system.css\"><link rel=\"stylesheet\" href=\"/onboarding-tour.css\"><link rel=\"stylesheet\" href=\"/profile-photo-quick.css\"><link rel=\"stylesheet\" href=\"/pet-identity-visuals.css\"><link rel=\"stylesheet\" href=\"/notification-center.css\"><link rel=\"stylesheet\" href=\"/mobile-navigation.css\"></head>");
    html=html.replace("</body>","<script src=\"/product-ux.js\"></script><script src=\"/profile-editor.js\"></script><script src=\"/profile-photo-social.js\"></script><script src=\"/social-experience.js\"></script><script src=\"/messaging.js\"></script><script src=\"/playdate-flow.js\"></script><script src=\"/health-passport.js\"></script><script src=\"/sitter-booking.js\"></script><script src=\"/memories-milestones.js\"></script><script src=\"/communities.js\"></script><script src=\"/smart-discovery.js\"></script><script src=\"/trust-safety.js\"></script><script src=\"/qa-polish.js\"></script><script src=\"/journey-flow.js\"></script><script src=\"/onboarding-tour.js\"></script><script src=\"/profile-photo-quick.js\"></script><script src=\"/pet-identity-visuals.js\"></script><script src=\"/universal-pet-identity.js\"></script><script src=\"/notification-center.js\"></script><script src=\"/pet-account-lifecycle.js\"></script><script src=\"/mobile-navigation.js\"></script></body>");
    return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
  }
}
