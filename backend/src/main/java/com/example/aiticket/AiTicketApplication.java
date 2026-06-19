package com.example.aiticket;

import com.example.aiticket.config.AiProviderProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AiProviderProperties.class)
public class AiTicketApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiTicketApplication.class, args);
    }
}
