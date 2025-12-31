package com.event.event_reservation_system.modele;

import lombok.Getter;

public enum Role {
    ADMIN("Administrateur", "#FF5722"),
    ORGANIZER("Organisateur", "#2196F3"),
    CLIENT("Client", "#4CAF50");

    private final String label;
    private final String color;

    Role(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public static Role fromString(String value) {
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Rôle invalide: " + value);
    }

    public String getColor() {
        return color;
    }
}
