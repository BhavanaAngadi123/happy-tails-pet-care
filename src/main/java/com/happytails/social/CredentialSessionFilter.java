package com.happytails.social;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 40)
public class CredentialSessionFilter extends OncePerRequestFilter {
 private final CredentialVersionService versions;
 public CredentialSessionFilter(CredentialVersionService versions){this.versions=versions;}

 @Override
 protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
  HttpSession session=request.getSession(false);
  if(session!=null){
   Object owner=session.getAttribute("ownerId");
   Object sessionVersion=session.getAttribute("credentialVersion");
   if(owner instanceof Long ownerId){
    long current=versions.current(ownerId);
    if(!(sessionVersion instanceof Long) || ((Long)sessionVersion)!=current){
     session.invalidate();
     if(request.getRequestURI().startsWith("/api/")){
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"Your session expired because account credentials changed. Please log in again.\"}");
      return;
     }
     response.sendRedirect("/login.html");
     return;
    }
   }
  }
  chain.doFilter(request,response);
 }
}
