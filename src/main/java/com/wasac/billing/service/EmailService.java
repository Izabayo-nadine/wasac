package com.wasac.billing.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Sends OTP verification emails via Gmail SMTP.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        if (!mailEnabled) {
            log.info("Mail disabled — OTP for {}: {}", toEmail, otp);
            return;
        }

        if (fromEmail == null || fromEmail.isBlank()) {
            log.warn("spring.mail.username is not set — OTP for {}: {}", toEmail, otp);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("WASAC/REG - Email Verification OTP");
            helper.setText(buildHtmlBody(otp), true);

            mailSender.send(mimeMessage);
            log.info("OTP email sent successfully to {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send OTP email to {}: {}", toEmail, ex.getMessage(), ex);
            log.info("Fallback OTP for {}: {}", toEmail, otp);
        }
    }

    private String buildHtmlBody(String otp) {
        return """
                <html><body style="font-family: Arial, sans-serif;">
                <h2>WASAC/REG Utility Billing</h2>
                <p>Your email verification code is:</p>
                <h1 style="letter-spacing: 4px; color: #1a5276;">%s</h1>
                <p>This code expires in 10 minutes.</p>
                <p>If you did not register, please ignore this email.</p>
                </body></html>
                """.formatted(otp);
    }
}
