package com.openisle.service;

import com.openisle.exception.EmailSendException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ResendEmailSender extends EmailSender {

  @Value("${resend.api.key}")
  private String apiKey;

  @Value("${resend.from.email}")
  private String fromEmail;

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public void sendEmail(String to, String subject, String text) {
    String url = "https://api.resend.com/emails"; // hypothetical endpoint

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + apiKey);

    Map<String, String> body = new HashMap<>();
    body.put("to", to);
    body.put("subject", subject);
    body.put("text", text);
    body.put("from", "openisle <" + fromEmail + ">");

    HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
    try {
      ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.POST,
        entity,
        String.class
      );
      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new EmailSendException(
          "Email service returned status " + response.getStatusCodeValue()
        );
      }
    } catch (RestClientException e) {
      throw new EmailSendException("Failed to send email: " + e.getMessage(), e);
    }
  }
}
