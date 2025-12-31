package com.event.event_reservation_system.exception;

// Requête invalide (400)
public class  BadRequestException extends BusinessException {
    public BadRequestException(String message) {
        super(message);
    }
}
