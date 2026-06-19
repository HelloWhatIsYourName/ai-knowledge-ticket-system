package com.example.aiticket.security;

import com.example.aiticket.config.JwtProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    @Test
    void createsAndParsesToken() {
        JwtService service = new JwtService(properties(7200));

        String token = service.createAccessToken(1L, "admin", 0);
        JwtClaims claims = service.parse(token);

        assertThat(claims.userId()).isEqualTo(1L);
        assertThat(claims.username()).isEqualTo("admin");
        assertThat(claims.tokenVersion()).isEqualTo(0);
    }

    @Test
    void rejectsExpiredToken() throws InterruptedException {
        JwtService service = new JwtService(properties(1));
        String token = service.createAccessToken(1L, "admin", 0);

        Thread.sleep(1200);

        assertThatThrownBy(() -> service.parse(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("invalid jwt token");
    }

    private JwtProperties properties(long expiresInSeconds) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("local-dev-secret-with-at-least-32-characters");
        properties.setExpiresInSeconds(expiresInSeconds);
        return properties;
    }
}
