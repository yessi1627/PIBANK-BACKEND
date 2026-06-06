package com.pibank.backend.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CvvGenerator {

    private static final SecureRandom random = new SecureRandom();

    public String generate() {
        return String.format("%03d", random.nextInt(1000));
    }

    public String generateCardNumber() {
        StringBuilder sb = new StringBuilder("4"); // Visa prefix
        for (int i = 0; i < 15; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
