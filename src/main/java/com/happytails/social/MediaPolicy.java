package com.happytails.social;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

final class MediaPolicy {
  private static final int MAX_MEDIA_CHARS = 1_000_000;

  private MediaPolicy() {}

  static String validateImage(String value) {
    if (value == null) return null;
    String media = value.trim();
    if (media.isEmpty()) return null;
    if (media.length() > MAX_MEDIA_CHARS) {
      throw new IllegalArgumentException("Image payload is too large.");
    }

    String lower = media.toLowerCase();
    boolean safeData = lower.startsWith("data:image/jpeg;base64,")
        || lower.startsWith("data:image/png;base64,")
        || lower.startsWith("data:image/webp;base64,");
    boolean safeRemote = lower.startsWith("https://");

    if (!safeData && !safeRemote) {
      throw new IllegalArgumentException("Only HTTPS images or JPG, PNG and WebP image uploads are allowed.");
    }
    return media;
  }
}

@RestControllerAdvice
class MediaPayloadExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<?> invalidMedia(IllegalArgumentException e) {
    String message = e.getMessage() == null ? "Invalid request." : e.getMessage();
    return ResponseEntity.badRequest().body(Map.of("error", message));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<?> unreadable(HttpMessageNotReadableException e) {
    Throwable cause = e.getMostSpecificCause();
    String message = cause != null && cause.getMessage() != null && cause.getMessage().contains("image")
        ? cause.getMessage()
        : "The request contains invalid data.";
    return ResponseEntity.badRequest().body(Map.of("error", message));
  }
}
