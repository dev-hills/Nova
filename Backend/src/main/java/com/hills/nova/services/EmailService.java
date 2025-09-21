package com.hills.nova.services;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otp, String firstName);
    void sendWelcomeEmail(String toEmail, String firstName);
}
