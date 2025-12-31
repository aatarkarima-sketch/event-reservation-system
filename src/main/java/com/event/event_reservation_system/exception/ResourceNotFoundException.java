package com.event.event_reservation_system.exception;

// Ressource non trouvée (404)
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s avec l'ID %d n'a pas été trouvé", resourceName, id));
    }
}
