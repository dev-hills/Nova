package com.hills.nova.services.impl;

import com.hills.nova.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    @Override
    public void sendOtpEmail(String toEmail, String otp, String firstName) {
        try {

            // Create Thymeleaf context
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("otp", otp);
            context.setVariable("appName", appName);
            context.setVariable("validityMinutes", 30);

            // Process the template
            String htmlContent = templateEngine.process("otp-email", context);

            sendEmailThroughBrevo(toEmail, "Verify Your Account - OTP Code", htmlContent);
            log.info("OTP email sent successfully to: {}", toEmail);


        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email", e);

        }
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {


            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("welcome-email", context);
            sendEmailThroughBrevo(toEmail, "Welcome to " + appName + "!", htmlContent);
            log.info("Welcome email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    private void sendEmailThroughBrevo(String toEmail, String subject, String htmlContent){
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of("email", fromEmail, "name", appName));
            body.put("to", new Object[]{Map.of("email", toEmail)});
            body.put("subject", subject);
            body.put("htmlContent", htmlContent);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_API_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("📨 Brevo API email sent successfully: {}", response.getBody());
            } else {
                log.error("⚠️ Brevo API failed: {}", response.getBody());
                throw new RuntimeException("Brevo API failed: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("❌ Error sending email via Brevo API", e);
            throw new RuntimeException("Error sending email via Brevo API", e);
        }
    }
}


