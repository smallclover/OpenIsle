package com.openisle.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AvatarGenerator {

  @Value("${app.avatar.base-url}")
  private String baseUrl;

  @Value("${app.avatar.style}")
  private String style;

  @Value("${app.avatar.size}")
  private int size;

  public String generate(String seed) {
    String encoded = URLEncoder.encode(seed, StandardCharsets.UTF_8);
    return String.format("%s/%s/png?seed=%s&size=%d", baseUrl, style, encoded, size);
  }
}
