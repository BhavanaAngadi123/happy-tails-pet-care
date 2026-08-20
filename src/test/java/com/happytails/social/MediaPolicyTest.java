package com.happytails.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MediaPolicyTest {
  @Test
  void acceptsSupportedImageInputs() {
    assertEquals("https://images.example.com/pet.jpg", MediaPolicy.validateImage("https://images.example.com/pet.jpg"));
    assertEquals("data:image/jpeg;base64,QUJD", MediaPolicy.validateImage("data:image/jpeg;base64,QUJD"));
    assertEquals("data:image/png;base64,QUJD", MediaPolicy.validateImage("data:image/png;base64,QUJD"));
    assertEquals("data:image/webp;base64,QUJD", MediaPolicy.validateImage("data:image/webp;base64,QUJD"));
  }

  @Test
  void rejectsUnsafeMediaSchemesAndSvgData() {
    assertThrows(IllegalArgumentException.class, () -> MediaPolicy.validateImage("javascript:alert(1)"));
    assertThrows(IllegalArgumentException.class, () -> MediaPolicy.validateImage("http://example.com/pet.jpg"));
    assertThrows(IllegalArgumentException.class, () -> MediaPolicy.validateImage("data:image/svg+xml;base64,PHN2Zz4="));
    assertThrows(IllegalArgumentException.class, () -> MediaPolicy.validateImage("data:text/html;base64,PGgxPkJvb208L2gxPg=="));
  }

  @Test
  void rejectsOversizedImagePayloads() {
    String huge = "data:image/jpeg;base64," + "A".repeat(1_000_000);
    assertThrows(IllegalArgumentException.class, () -> MediaPolicy.validateImage(huge));
  }
}
