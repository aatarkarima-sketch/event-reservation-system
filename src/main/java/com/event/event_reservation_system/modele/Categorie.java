package com.event.event_reservation_system.modele;
import lombok.Getter;

@Getter
public enum Categorie {
    CONCERT("Concert", "🎵", "#E91E63"),
    THEATRE("Théâtre", "🎭", "#9C27B0"),
    CONFERENCE("Conférence", "🎤", "#3F51B5"),
    SPORT("Sport", "⚽", "#4CAF50"),
    AUTRE("Autre", "📅", "#607D8B");

    private final String label;
    private final String icon;
    private final String color;

    Categorie(String label, String icon, String color) {
        this.label = label;
        this.icon = icon;
        this.color = color;
    }

    public String getDisplayName() {
        return icon + " " + label;
    }

    public static Categorie fromString(String value) {
        for (Categorie cat : Categorie.values()) {
            if (cat.name().equalsIgnoreCase(value) || cat.label.equalsIgnoreCase(value)) {
                return cat;
            }
        }
        throw new IllegalArgumentException("Catégorie invalide: " + value);
    }
}
