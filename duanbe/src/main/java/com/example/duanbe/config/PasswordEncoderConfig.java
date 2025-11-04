package com.example.duanbe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configuration class for Password Encoder
 * Provides BCryptPasswordEncoder bean for password hashing
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Creates BCryptPasswordEncoder bean
     * BCrypt is a strong hashing function designed for passwords
     * Default strength is 10 rounds
     * 
     * @return BCryptPasswordEncoder instance
     *
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
