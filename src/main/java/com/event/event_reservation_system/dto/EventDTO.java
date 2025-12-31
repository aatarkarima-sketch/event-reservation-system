package com.event.event_reservation_system.dto;

import com.event.event_reservation_system.modele.Categorie;
import com.event.event_reservation_system.modele.Event;
import com.event.event_reservation_system.modele.Statut;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour transférer les données d'événement sans les relations lazy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {

    private Long id;
    private String titre;
    private String description;
    private Categorie categorie;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String lieu;
    private String ville;
    private Integer capaciteMax;
    private Double prixUnitaire;
    private String imageUrl;
    private Statut statut;
    private String organisateurNom;
    private String organisateurEmail;
    private Integer placesReservees;
    private Integer placesDisponibles;

    /**
     * Convertir une entité Event en DTO
     */
    public static EventDTO fromEntity(Event event, int placesReservees) {
        return EventDTO.builder()
                .id(event.getId())
                .titre(event.getTitre())
                .description(event.getDescription())
                .categorie(event.getCategorie())
                .dateDebut(event.getDateDebut())
                .dateFin(event.getDateFin())
                .lieu(event.getLieu())
                .ville(event.getVille())
                .capaciteMax(event.getCapaciteMax())
                .prixUnitaire(event.getPrixUnitaire())
                .imageUrl(event.getImageUrl())
                .statut(event.getStatut())
                .organisateurNom(event.getOrganisateur().getNomComplet())
                .organisateurEmail(event.getOrganisateur().getEmail())
                .placesReservees(placesReservees)
                .placesDisponibles(event.getCapaciteMax() - placesReservees)
                .build();
    }

    public boolean isDisponible() {
        return statut == Statut.PUBLIE &&
                LocalDateTime.now().isBefore(dateFin);
    }

    public boolean isComplet() {
        return placesDisponibles <= 0;
    }

    public double getTauxRemplissage() {
        if (capaciteMax == null || capaciteMax == 0) {
            return 0;
        }
        return (double) placesReservees / capaciteMax * 100;
    }
}
