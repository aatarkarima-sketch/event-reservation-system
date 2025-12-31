package com.event.event_reservation_system.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Non autorisé (401)
class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("Vous devez être connecté pour effectuer cette action");
    }
}

