package com.event.event_reservation_system.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.function.Supplier;

@Component
public class CodeGenerator {
    private static final String PREFIX = "EVT-";
    private static final int CODE_LENGTH = 5;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Génère un code de réservation unique
     * @param existsChecker fonction pour vérifier l'existence du code
     * @return code unique au format EVT-XXXXX
     */
    public String generateUniqueCode(Supplier<Boolean> existsChecker) {
        String code;
        int attempts = 0;
        final int MAX_ATTEMPTS = 100;

        do {
            code = generateCode();
            attempts++;

            if (attempts >= MAX_ATTEMPTS) {
                throw new IllegalStateException(
                        "Impossible de générer un code unique après " + MAX_ATTEMPTS + " tentatives"
                );
            }
        } while (existsChecker.get());

        return code;
    }

    /**
     * Génère un code aléatoire
     */
    private String generateCode() {
        StringBuilder code = new StringBuilder(PREFIX);

        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }

        return code.toString();
    }

    /**
     * Valide le format d'un code de réservation
     */
    public boolean isValidFormat(String code) {
        if (code == null || code.length() != PREFIX.length() + CODE_LENGTH) {
            return false;
        }

        if (!code.startsWith(PREFIX)) {
            return false;
        }

        String numericPart = code.substring(PREFIX.length());
        return numericPart.matches("\\d{" + CODE_LENGTH + "}");
    }

}
