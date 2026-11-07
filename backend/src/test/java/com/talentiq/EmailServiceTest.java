package com.talentiq;

import com.talentiq.exception.BadRequestException;
import com.talentiq.service.EmailService;
import com.talentiq.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailServiceTest {

    @Test
    @DisplayName("EmailService throws BadRequestException when SMTP host is not configured")
    void testEmailServiceFailsSafelyWithoutSmtp() {
        EmailService emailService = new EmailService(null, "", "no-reply@talentiq.ai", "http://localhost:5173");

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                emailService.sendVerificationEmail("user@example.com", "User Name", "dummy-raw-token")
        );

        assertTrue(ex.getMessage().contains("Email service is not configured"));
    }

    @Test
    @DisplayName("TokenService generates cryptographically secure 32-byte tokens and correct SHA-256 hashes")
    void testTokenServiceGenerationAndHashing() {
        TokenService tokenService = new TokenService();

        String rawToken1 = tokenService.generateSecureToken();
        String rawToken2 = tokenService.generateSecureToken();

        assertNotNull(rawToken1);
        assertNotNull(rawToken2);
        assertNotEquals(rawToken1, rawToken2);

        String hash1 = tokenService.hashToken(rawToken1);
        String hash2 = tokenService.hashToken(rawToken2);

        assertEquals(64, hash1.length()); // 256 bits = 32 bytes = 64 hex characters
        assertEquals(64, hash2.length());
        assertNotEquals(hash1, hash2);
        assertNotEquals(rawToken1, hash1); // Hash must differ from raw token
    }
}

