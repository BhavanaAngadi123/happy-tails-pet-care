package com.happytails.social;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class SameOriginWriteFilter extends OncePerRequestFilter {
  private static final Set<String> SAFE = Set.of("GET", "HEAD", "OPTIONS");

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (!request.getRequestURI().startsWith("/api/") || SAFE.contains(request.getMethod())) {
      chain.doFilter(request, response);
      return;
    }

    String source = firstNonBlank(request.getHeader("Origin"), request.getHeader("Referer"));
    if (source != null && !sameHost(source, request)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"Cross-site write request blocked.\"}");
      return;
    }

    chain.doFilter(request, response);
  }

  private boolean sameHost(String source, HttpServletRequest request) {
    try {
      URI uri = URI.create(source.trim());
      String sourceHost = normalizeHost(uri.getHost());
      if (sourceHost == null) return false;

      String expected = firstNonBlank(request.getHeader("X-Forwarded-Host"), request.getHeader("Host"));
      if (expected == null) expected = request.getServerName();
      expected = expected.split(",")[0].trim();
      if (expected.startsWith("[")) {
        int end = expected.indexOf(']');
        expected = end >= 0 ? expected.substring(1, end) : expected;
      } else {
        int colon = expected.indexOf(':');
        if (colon >= 0) expected = expected.substring(0, colon);
      }
      return sourceHost.equals(normalizeHost(expected));
    } catch (Exception ignored) {
      return false;
    }
  }

  private String normalizeHost(String host) {
    if (host == null || host.isBlank()) return null;
    String h = host.trim().toLowerCase(Locale.ROOT);
    return h.endsWith(".") ? h.substring(0, h.length() - 1) : h;
  }

  private String firstNonBlank(String... values) {
    for (String value : values) if (value != null && !value.isBlank()) return value;
    return null;
  }
}
