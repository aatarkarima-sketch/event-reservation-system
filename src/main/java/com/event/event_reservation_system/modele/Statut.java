package com.event.event_reservation_system.modele;

import lombok.Getter;

@Getter
public enum Statut {
    BROUILLON("Brouillon", "#9E9E9E", "draft"),
    PUBLIE("Publié", "#4CAF50", "check_circle"),
    ANNULE("Annulé", "#F44336", "cancel"),
    TERMINE("Terminé", "#607D8B", "event_available");

    private final String label;
    private final String color;
    private final String icon;

    Statut(String label, String color, String icon) {
        this.label = label;
        this.color = color;
        this.icon = icon;
    }

    public boolean peutTransitionnerVers(Statut nouveauStatut) {
        return switch (this) {
            case BROUILLON -> nouveauStatut == PUBLIE || nouveauStatut == ANNULE;
            case PUBLIE -> nouveauStatut == ANNULE || nouveauStatut == TERMINE;
            case ANNULE, TERMINE -> false;
        };
    }

    public static Statut fromString(String value) {
        for (Statut statut : Statut.values()) {
            if (statut.name().equalsIgnoreCase(value)) {
                return statut;
            }
        }
        throw new IllegalArgumentException("Statut invalide: " + value);
    }
}
