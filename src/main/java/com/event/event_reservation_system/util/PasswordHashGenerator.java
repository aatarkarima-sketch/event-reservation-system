package com.event.event_reservation_system.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordHashGenerator {

    @Bean
    public CommandLineRunner generatePasswordHash() {
        return args -> {
            PasswordEncoder encoder = new BCryptPasswordEncoder();
            String rawPassword = "Password123";
            String hashedPassword = encoder.encode(rawPassword);

            System.out.println("========================================");
            System.out.println("GÉNÉRATEUR DE HASH BCrypt");
            System.out.println("========================================");
            System.out.println("Mot de passe en clair : " + rawPassword);
            System.out.println("Hash BCrypt           : " + hashedPassword);
            System.out.println("========================================");
            System.out.println("\nCopiez ce hash dans votre data.sql !\n");
        };
    }
}