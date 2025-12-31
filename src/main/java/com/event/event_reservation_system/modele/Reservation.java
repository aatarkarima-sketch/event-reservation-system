package com.event.event_reservation_system.modele;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    @NotNull(message = "L'utilisateur est obligatoire")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id", nullable = false)
    @NotNull(message = "L'événement est obligatoire")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Event evenement;

    @NotNull(message = "Le nombre de places est obligatoire")
    @Min(value = 1, message = "Le nombre de places doit être au moins 1")
    @Max(value = 10, message = "Le nombre de places ne peut pas dépasser 10")
    @Column(nullable = false)
    private Integer nombrePlaces;

    @Column(nullable = false)
    private Double montantTotal;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateReservation = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutReservation statut = StatutReservation.EN_ATTENTE;

    @Column(nullable = false, unique = true)
    private String codeReservation;

    @Size(max = 500, message = "Le commentaire ne peut pas dépasser 500 caractères")
    @Column(length = 500)
    private String commentaire;

    // Méthode de callback pour calculer le montant avant persistance
    @PrePersist
    @PreUpdate
    protected void calculerMontant() {
        if (evenement != null && nombrePlaces != null) {
            this.montantTotal = evenement.getPrixUnitaire() * nombrePlaces;
        }
    }

    // Méthodes métier
    public boolean peutEtreAnnulee() {
        if (statut == StatutReservation.ANNULEE) {
            return false;
        }

        // Vérifier les 48h avant l'événement
        LocalDateTime limite = evenement.getDateDebut().minus(48, ChronoUnit.HOURS);
        return LocalDateTime.now().isBefore(limite);
    }

    public long getHeuresAvantEvenement() {
        return ChronoUnit.HOURS.between(LocalDateTime.now(), evenement.getDateDebut());
    }

    public boolean isConfirmee() {
        return statut == StatutReservation.CONFIRMEE;
    }

    public boolean isAnnulee() {
        return statut == StatutReservation.ANNULEE;
    }

    public boolean isEnAttente() {
        return statut == StatutReservation.EN_ATTENTE;
    }

    public String getRecapitulatif() {
        return String.format(
                "Réservation %s\n" +
                        "Événement: %s\n" +
                        "Date: %s\n" +
                        "Places: %d\n" +
                        "Montant: %.2f DH\n" +
                        "Statut: %s",
                codeReservation,
                evenement.getTitre(),
                evenement.getDateDebut(),
                nombrePlaces,
                montantTotal,
                statut.getLabel()
        );
    }
}
