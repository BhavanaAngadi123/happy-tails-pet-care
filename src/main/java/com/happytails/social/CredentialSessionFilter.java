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
  HttpSession before=request.getSession(false);
  if(before!=null){
   Object owner=before.getAttribute("ownerId");
   Object sessionVersion=before.getAttribute("credentialVersion");
   if(owner instanceof Long ownerId){
    long current=versions.current(ownerId);
    if(sessionVersion instanceof Long && ((Long)sessionVersion)!=current){
     before.invalidate();
     reject(request,response);
     return;
    }
    if(sessionVersion==null && !isAuthenticationEntry(request)){
     before.invalidate();
     reject(request,response);
     return;
    }
   }
  }

  chain.doFilter(request,response);

  if(response.getStatus()>=200&&response.getStatus()<300){
   HttpSession after=request.getSession(false);
   if(after==null)return;
   Object owner=after.getAttribute("ownerId");
   if(!(owner instanceof Long ownerId))return;
   String path=request.getRequestURI();
   if("POST".equals(request.getMethod())&&"/api/auth/change-password".equals(path)){
    after.setAttribute("credentialVersion",versions.rotate(ownerId));
   }else if(isAuthenticationEntry(request)||after.getAttribute("credentialVersion")==null){
    after.setAttribute("credentialVersion",versions.current(ownerId));
   }
  }
 }

 private boolean isAuthenticationEntry(HttpServletRequest request){
  if(!"POST".equals(request.getMethod()))return false;
  String path=request.getRequestURI();
  return "/api/auth/login".equals(path)||"/api/auth/signup".equals(path);
 }

 private void reject(HttpServletRequest request,HttpServletResponse response)throws IOException{
  if(request.getRequestURI().startsWith("/api/")){
   response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
   response.setContentType("application/json");
   response.getWriter().write("{\"error\":\"Your session expired because account credentials changed. Please log in again.\"}");
  }else response.sendRedirect("/login.html");
 }
}
