package com.happytails.social;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestPayloadLimitFilter extends OncePerRequestFilter {
  static final long MAX_API_BODY_BYTES = 1_500_000L;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri=request.getRequestURI();
    return uri==null || !uri.startsWith("/api/") || !("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod()) || "PATCH".equalsIgnoreCase(request.getMethod()));
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
    long declared=request.getContentLengthLong();
    if(declared>MAX_API_BODY_BYTES){
      reject(response);
      return;
    }
    chain.doFilter(request,response);
  }

  private void reject(HttpServletResponse response) throws IOException {
    response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
    response.setContentType("application/json");
    response.getWriter().write("{\"error\":\"Request payload is too large. Images must be compressed before upload.\"}");
  }
}
