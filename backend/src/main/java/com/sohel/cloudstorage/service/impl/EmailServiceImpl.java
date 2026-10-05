package com.sohel.cloudstorage.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.sohel.cloudstorage.service.EmailService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;
        log.info("Sending Verification Email to {} with link: {}", toEmail, verificationUrl);

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("CloudStorage - Verify Your Email");
                message.setText("Click the following link to verify your email address:\n" + verificationUrl);
                mailSender.send(message);
            } catch (Exception e) {
                log.error("Failed to send verification email via JavaMailSender: {}", e.getMessage());
            }
        }
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        log.info("Sending Password Reset Email to {} with link: {}", toEmail, resetUrl);

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("CloudStorage - Reset Password Request");
                message.setText("Click the following link to reset your password:\n" + resetUrl);
                mailSender.send(message);
            } catch (Exception e) {
                log.error("Failed to send reset password email via JavaMailSender: {}", e.getMessage());
            }
        }
    }
}
