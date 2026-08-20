package com.happytails.social;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AbuseRateLimitFilter extends OncePerRequestFilter {
  private static final long WINDOW_MS = 60_000L;
  private static final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  static class Bucket {
    long windowStart;
    int count;
    Bucket(long now){windowStart=now;}
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    String method=request.getMethod();
    String path=request.getRequestURI();
    int limit=limitFor(method,path);
    if(limit<=0){chain.doFilter(request,response);return;}

    String key=clientKey(request)+"|"+method+"|"+bucketPath(path);
    long now=Instant.now().toEpochMilli();
    Bucket b=buckets.computeIfAbsent(key,k->new Bucket(now));
    boolean blocked;
    synchronized(b){
      if(now-b.windowStart>=WINDOW_MS){b.windowStart=now;b.count=0;}
      b.count++;
      blocked=b.count>limit;
    }

    if(blocked){
      response.setStatus(429);
      response.setContentType("application/json");
      response.setHeader("Retry-After","60");
      response.getWriter().write("{\"error\":\"Too many requests. Please wait a moment and try again.\"}");
      cleanupOccasionally(now);
      return;
    }
    cleanupOccasionally(now);
    chain.doFilter(request,response);
  }

  private int limitFor(String method,String path){
    if("POST".equals(method)&&"/api/auth/login".equals(path))return 10;
    if("POST".equals(method)&&"/api/auth/signup".equals(path))return 6;
    if(!path.startsWith("/api/"))return 0;
    if("GET".equals(method)||"HEAD".equals(method)||"OPTIONS".equals(method))return 0;
    if(path.startsWith("/api/messages/"))return 40;
    if(path.contains("/paw"))return 60;
    if(path.contains("/comments"))return 30;
    if(path.contains("/friend-requests"))return 20;
    if(path.contains("/follows"))return 30;
    if(path.contains("/play-dates"))return 20;
    if(path.contains("/meetups"))return 20;
    if(path.contains("/orders"))return 15;
    if(path.contains("/posts"))return 25;
    return 50;
  }

  private String bucketPath(String path){
    if(path.startsWith("/api/auth/login"))return "/api/auth/login";
    if(path.startsWith("/api/auth/signup"))return "/api/auth/signup";
    return path.replaceAll("/\\d+","/{id}");
  }

  private String clientKey(HttpServletRequest r){
    String forwarded=r.getHeader("X-Forwarded-For");
    if(forwarded!=null&&!forwarded.isBlank())return forwarded.split(",")[0].trim();
    String remote=r.getRemoteAddr();
    return remote==null?"unknown":remote;
  }

  private void cleanupOccasionally(long now){
    if((now/1000)%30!=0)return;
    buckets.entrySet().removeIf(e->now-e.getValue().windowStart>5*WINDOW_MS);
  }
}
