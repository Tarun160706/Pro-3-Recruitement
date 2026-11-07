package com.talentiq.service;

import com.talentiq.exception.BadRequestException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String mailHost;
    private final String mailFrom;
    private final String frontendUrl;

    public EmailService(
            @Autowired(required = false) JavaMailSender mailSender,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${talentiq.mail.from:no-reply@talentiq.ai}") String mailFrom,
            @Value("${talentiq.app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.mailSender = mailSender;
        this.mailHost = mailHost != null ? mailHost.trim() : "";
        this.mailFrom = mailFrom;
        this.frontendUrl = frontendUrl;
    }

    public void sendVerificationEmail(String toEmail, String fullName, String rawToken) {
        // Enforce fail-safe behavior: do not bypass verification and do not log raw tokens
        if (mailSender == null || mailHost.isEmpty()) {
            log.warn("Email service cannot send email: SMTP is not configured. Target: {}", maskEmail(toEmail));
            throw new BadRequestException("Email service is not configured. SMTP settings must be configured to send verification emails.");
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject("Verify your TalentIQ Account");

            String verificationUrl = frontendUrl + "/verify-email?token=" + rawToken;
            String html = buildEmailTemplate(fullName, verificationUrl);

            helper.setText(html, true);
            mailSender.send(mimeMessage);

            log.info("Verification email successfully sent to {}", maskEmail(toEmail));
        } catch (Exception ex) {
            log.error("Failed to send verification email to {}: {}", maskEmail(toEmail), ex.getMessage());
            throw new BadRequestException("Failed to send verification email. Please verify SMTP settings and try again.");
        }
    }

    private String buildEmailTemplate(String fullName, String verificationUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; color: #1e293b; }
                    .card { max-width: 540px; margin: 0 auto; background: #ffffff; border-radius: 12px; border: 1px solid #e2e8f0; padding: 32px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
                    .header { font-size: 22px; font-weight: 700; color: #0f172a; margin-bottom: 12px; }
                    .btn { display: inline-block; padding: 12px 24px; font-size: 15px; font-weight: 600; color: #ffffff !important; background-color: #2563eb; text-decoration: none; border-radius: 8px; margin: 24px 0; }
                    .footer { font-size: 13px; color: #64748b; margin-top: 24px; border-top: 1px solid #f1f5f9; padding-top: 16px; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <div class="header">Welcome to TalentIQ</div>
                    <p>Hello %s,</p>
                    <p>Thank you for registering with TalentIQ. Please click the button below to verify your email address and activate your account.</p>
                    <p style="text-align: center;">
                      <a href="%s" class="btn" target="_blank">Verify Email Address</a>
                    </p>
                    <p>This verification link will expire in 24 hours. If you did not create a TalentIQ account, no further action is required.</p>
                    <div class="footer">
                      TalentIQ Intelligence Platform &bull; Secure Authentication System
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(fullName != null ? fullName : "User", verificationUrl);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        String prefix = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (prefix.length() <= 2) {
            return prefix.charAt(0) + "***" + domain;
        }
        return prefix.charAt(0) + "***" + prefix.charAt(prefix.length() - 1) + domain;
    }
}

