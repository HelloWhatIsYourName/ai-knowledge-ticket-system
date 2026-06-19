package com.example.aiticket.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesTest {
    @Test
    void bindsJwtSettings() throws Exception {
        String yaml = """
                security:
                  jwt:
                    secret: local-dev-secret-with-at-least-32-characters
                    expires-in-seconds: 7200
                """;

        StandardEnvironment environment = new StandardEnvironment();
        MutablePropertySources sources = environment.getPropertySources();
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        sources.addFirst(loader.load("test", new ByteArrayResource(yaml.getBytes(StandardCharsets.UTF_8))).getFirst());

        JwtProperties properties = Binder.get(environment)
                .bind("security.jwt", Bindable.of(JwtProperties.class))
                .orElseThrow(() -> new IllegalStateException("jwt properties did not bind"));

        assertThat(properties.getSecret()).isEqualTo("local-dev-secret-with-at-least-32-characters");
        assertThat(properties.getExpiresInSeconds()).isEqualTo(7200);
    }
}
