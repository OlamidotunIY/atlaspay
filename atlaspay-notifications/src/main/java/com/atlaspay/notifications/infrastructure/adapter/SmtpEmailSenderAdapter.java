package com.atlaspay.notifications.infrastructure.adapter;

import com.atlaspay.notifications.application.port.EmailSenderPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Year;

@Component
public class SmtpEmailSenderAdapter implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSenderAdapter.class);

    private final JavaMailSender javaMailSender;

    @Value("")
    private String fromEmail;

    public SmtpEmailSenderAdapter(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendVerificationEmail(String toEmail, String verificationCode) {
        log.info("Preparing to send HTML verification email to {}", toEmail);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("AtlasPay - Verify your email address");
            
            String htmlContent = buildVerificationEmailHtml(verificationCode);
            helper.setText(htmlContent, true);
            
            javaMailSender.send(message);
            log.info("Successfully sent HTML verification email to {}", toEmail);
        } catch (MessagingException e) {
            log.error("MessagingException occurred while constructing/sending verification email to {}", toEmail, e);
        } catch (Exception e) {
            log.error("Unexpected error occurred while sending verification email to {}", toEmail, e);
        }
    }

    @Override
    public void sendAdminWelcomeEmail(String toEmail, String temporaryPassword) {
        log.info("Preparing to send HTML admin welcome email to {}", toEmail);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to AtlasPay - Admin Account Created");
            
            String htmlContent = buildAdminWelcomeEmailHtml(temporaryPassword);
            helper.setText(htmlContent, true);
            
            javaMailSender.send(message);
            log.info("Successfully sent HTML admin welcome email to {}", toEmail);
        } catch (MessagingException e) {
            log.error("MessagingException occurred while constructing/sending admin welcome email to {}", toEmail, e);
        } catch (Exception e) {
            log.error("Unexpected error occurred while sending admin welcome email to {}", toEmail, e);
        }
    }

    private String buildVerificationEmailHtml(String verificationCode) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
                        background-color: #f4f7f6;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 40px auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        overflow: hidden;
                        box-shadow: 0 4px 15px rgba(0,0,0,0.05);
                    }
                    .header {
                        background-color: #0d1117;
                        padding: 30px 20px;
                        text-align: center;
                    }
                    .header h1 {
                        color: #ffffff;
                        margin: 0;
                        font-size: 24px;
                        letter-spacing: 1px;
                    }
                    .content {
                        padding: 40px 30px;
                        color: #333333;
                        line-height: 1.6;
                    }
                    .content h2 {
                        font-size: 20px;
                        color: #1a1a1a;
                        margin-top: 0;
                    }
                    .code-box {
                        background-color: #f8f9fa;
                        border: 1px dashed #ced4da;
                        border-radius: 6px;
                        padding: 20px;
                        text-align: center;
                        margin: 30px 0;
                    }
                    .code {
                        font-size: 32px;
                        font-weight: bold;
                        color: #0056b3;
                        letter-spacing: 5px;
                        margin: 0;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        font-size: 13px;
                        color: #6c757d;
                        border-top: 1px solid #eeeeee;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>ATLASPAY</h1>
                    </div>
                    <div class="content">
                        <h2>Verify Your Email Address</h2>
                        <p>Welcome to AtlasPay! We're excited to have you on board.</p>
                        <p>To continue setting up your account, please enter the following verification code:</p>
                        
                        <div class="code-box">
                            <p class="code">%s</p>
                        </div>
                        
                        <p>This code will expire in 24 hours.</p>
                        <p>If you didn't request this email, you can safely ignore it.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; %d AtlasPay Inc. All rights reserved.</p>
                        <p>This is an automated message, please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(verificationCode, Year.now().getValue());
    }

    private String buildAdminWelcomeEmailHtml(String temporaryPassword) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
                        background-color: #f4f7f6;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 40px auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        overflow: hidden;
                        box-shadow: 0 4px 15px rgba(0,0,0,0.05);
                    }
                    .header {
                        background-color: #0d1117;
                        padding: 30px 20px;
                        text-align: center;
                    }
                    .header h1 {
                        color: #ffffff;
                        margin: 0;
                        font-size: 24px;
                        letter-spacing: 1px;
                    }
                    .content {
                        padding: 40px 30px;
                        color: #333333;
                        line-height: 1.6;
                    }
                    .content h2 {
                        font-size: 20px;
                        color: #1a1a1a;
                        margin-top: 0;
                    }
                    .code-box {
                        background-color: #f8f9fa;
                        border: 1px dashed #ced4da;
                        border-radius: 6px;
                        padding: 20px;
                        text-align: center;
                        margin: 30px 0;
                    }
                    .code {
                        font-size: 24px;
                        font-weight: bold;
                        color: #dc3545;
                        letter-spacing: 2px;
                        margin: 0;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        font-size: 13px;
                        color: #6c757d;
                        border-top: 1px solid #eeeeee;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>ATLASPAY</h1>
                    </div>
                    <div class="content">
                        <h2>Welcome to the AtlasPay Admin Team</h2>
                        <p>An administrative account has been created for you.</p>
                        <p>Please use your employee code and the following temporary password to log in. You will be required to change this password upon your first login:</p>
                        
                        <div class="code-box">
                            <p class="code">%s</p>
                        </div>
                        
                        <p>Please keep this password secure and do not share it with anyone.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; %d AtlasPay Inc. All rights reserved.</p>
                        <p>This is an automated message, please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(temporaryPassword, Year.now().getValue());
    }
}

