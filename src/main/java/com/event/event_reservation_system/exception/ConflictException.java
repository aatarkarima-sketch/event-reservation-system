package com.event.event_reservation_system.exception;

// Conflit (409)
public class ConflictException extends BusinessException {
    public ConflictException(String message) {
        super(message);
    }
}
