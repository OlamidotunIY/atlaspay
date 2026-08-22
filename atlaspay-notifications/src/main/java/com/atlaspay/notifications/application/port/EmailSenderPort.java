package com.atlaspay.notifications.application.port;

public interface EmailSenderPort {
    void sendVerificationEmail(String toEmail, String verificationCode);
    void sendAdminWelcomeEmail(String toEmail, String temporaryPassword);
}

