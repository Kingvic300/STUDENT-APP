package com.izabi.service;

import com.izabi.exception.EmailCannotBeBlankException;
import com.izabi.exception.EmailNotSentException;
import sibApi.TransactionalEmailsApi;
import sibModel.*;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;

import sibApi.TransactionalEmailsApi;
import sibModel.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    private static final int OTP_EXPIRY_MINUTES = 2;

    @Async
    @Override
    public void sendEmail(String toEmail, String otp) {
        if (toEmail == null || toEmail.isBlank()) {
            throw new EmailCannotBeBlankException("Recipient email address cannot be blank or null");
        }
        String subject = "Your OTP Code";
        String htmlContent = buildOtpHtmlTemplate(otp);
        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendResetPasswordEmail(String toEmail, String otp) {
        if (toEmail == null || toEmail.isBlank()) {
            throw new EmailCannotBeBlankException("Recipient email address cannot be blank or null");
        }
        String subject = "Reset Your Password";
        String htmlContent = buildResetPasswordHtmlTemplate(otp);
        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String content) {
        try {
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            apiKey.setApiKey(brevoApiKey);

            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(fromEmail);
            sender.setName("IZABI");

            List<SendSmtpEmailTo> recipients = new ArrayList<>();
            recipients.add(new SendSmtpEmailTo().email(to));

            SendSmtpEmail email = new SendSmtpEmail();
            email.setSender(sender);
            email.setTo(recipients);
            email.setSubject(subject);
            email.setHtmlContent(content);

            apiInstance.sendTransacEmail(email);
            log.info("Email successfully sent to {}", to);

        } catch (Exception e) {
            log.error("Failed to send email via Brevo to {}: {}", to, e.getMessage());
            throw new EmailNotSentException("Failed to send email. Please try again later.");
        }
    }

    private String buildOtpHtmlTemplate(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Your OTP Code</title>
                </head>
                <body style="font-family: Arial, sans-serif;">
                    <div style="max-width: 600px; margin: auto; padding: 20px; background-color: #f4f4f4; border-radius: 10px;">
                        <h2 style="color: #333;">Your One-Time Password (OTP)</h2>
                        <p>Please use the OTP below to complete your action:</p>
                        <p style="font-size: 24px; font-weight: bold; color: #007BFF;">%s</p>
                        <p>This OTP is valid for <strong>%d minutes</strong>.</p>
                        <p>If you did not request this, you can safely ignore this email.</p>
                        <br/>
                        <p>Regards,<br/>IZABI Team</p>
                    </div>
                </body>
                </html>
                """.formatted(otp, OTP_EXPIRY_MINUTES);
    }

    private String buildResetPasswordHtmlTemplate(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Reset Your Password</title>
                </head>
                <body style="font-family: Arial, sans-serif;">
                    <div style="max-width: 600px; margin: auto; padding: 20px; background-color: #fefefe; border-radius: 10px;">
                        <h2 style="color: #333;">Reset Password Request</h2>
                        <p>Use the OTP below to reset your password:</p>
                        <p style="font-size: 24px; font-weight: bold; color: #DC3545;">%s</p>
                        <p>This code will expire in <strong>%d minutes</strong>.</p>
                        <p>If you didn't request this, ignore this email.</p>
                        <br/>
                        <p>Regards,<br/>IZABI Security</p>
                    </div>
                </body>
                </html>
                """.formatted(otp, OTP_EXPIRY_MINUTES);
    }
}
