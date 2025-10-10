package com.hills.nova.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @GetMapping("/test-brevo-email")
    public String testBrevoEmail() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.brevo.com/v3/smtp/email";

            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of("email", "hilaryemujede48@gmail.com"));
            body.put("to", new Object[]{Map.of("email", "hilaryemujede48@gmail.com")});
            body.put("subject", "Test Email from Brevo API");
            body.put("htmlContent", "<p>This is a test email sent via Brevo API!</p>");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey); // 🔒 use env var

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            return "Brevo API response: " + response.getBody();
        } catch (Exception e) {
            return "Brevo email failed: " + e.getMessage();
        }
    }
}
