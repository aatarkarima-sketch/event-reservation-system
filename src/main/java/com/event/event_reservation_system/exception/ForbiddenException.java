package com.event.event_reservation_system.exception;

// Accès interdit (403)
public class ForbiddenException extends BusinessException {
    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException() {
        super("Vous n'avez pas les droits nécessaires pour effectuer cette action");
    }
}
