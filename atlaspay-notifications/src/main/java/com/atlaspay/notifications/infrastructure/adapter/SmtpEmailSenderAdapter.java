package com.atlaspay.notifications.infrastructure.adapter;

import com.atlaspay.notifications.application.port.EmailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpEmailSenderAdapter implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSenderAdapter.class);

    private final JavaMailSender javaMailSender;

    public SmtpEmailSenderAdapter(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendVerificationEmail(String toEmail, String verificationCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@atlaspay.com");
            message.setTo(toEmail);
            message.setSubject("AtlasPay - Verify your email address");
            message.setText("Welcome to AtlasPay!\n\nYour email verification code is: " + verificationCode + 
                            "\n\nThis code will expire in 24 hours.");
            
            javaMailSender.send(message);
            log.info("Sent verification email to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}", toEmail, e);
        }
    }
}
