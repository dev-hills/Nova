package com.hills.nova.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/test-brevo-email")
    public String testBrevoEmail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("hilaryemujede48@gmail.com");  // Your verified sender
            message.setTo("hilaryemujede48@gmail.com");  // Test recipient
            message.setSubject("Test Email from Brevo");
            message.setText("This is a test email sent via Brevo SMTP!");

            mailSender.send(message);
            return "Brevo email sent successfully!";
        } catch (Exception e) {
            return "Brevo email failed: " + e.getMessage();
        }
    }
}
