package com.event.event_reservation_system.modele;

public enum StatutReservation {
    EN_ATTENTE("En attente", "#FF9800", "schedule"),
    CONFIRMEE("Confirmée", "#4CAF50", "check_circle"),
    ANNULEE("Annulée", "#F44336", "cancel");

    private final String label;
    private final String color;
    private final String icon;

    StatutReservation(String label, String color, String icon) {
        this.label = label;
        this.color = color;
        this.icon = icon;
    }

    public boolean peutEtreAnnulee() {
        return this != ANNULEE;
    }

    public static StatutReservation fromString(String value) {
        for (StatutReservation statut : StatutReservation.values()) {
            if (statut.name().equalsIgnoreCase(value)) {
                return statut;
            }
        }
        throw new IllegalArgumentException("Statut de réservation invalide: " + value);
    }
    public String getLabel() {
        return label;
    }
    public String getColor() {
        return color;
    }
    public String getIcon() {
        return icon;
    }

}
