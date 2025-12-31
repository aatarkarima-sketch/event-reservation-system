package com.event.event_reservation_system.modele;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 100, message = "Le titre doit contenir entre 5 et 100 caractères")
    @Column(nullable = false)
    private String titre;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "La catégorie est obligatoire")
    @Column(nullable = false)
    private Categorie categorie;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(nullable = false)
    private LocalDateTime dateFin;

    @NotBlank(message = "Le lieu est obligatoire")
    @Column(nullable = false)
    private String lieu;

    @NotBlank(message = "La ville est obligatoire")
    @Column(nullable = false)
    private String ville;

    @NotNull(message = "La capacité maximale est obligatoire")
    @Min(value = 1, message = "La capacité doit être supérieure à 0")
    @Column(nullable = false)
    private Integer capaciteMax;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", message = "Le prix doit être positif ou nul")
    @Column(nullable = false)
    private Double prixUnitaire;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisateur_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User organisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Statut statut = Statut.BROUILLON;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime dateModification = LocalDateTime.now();

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Reservation> reservations = new ArrayList<>();

    // Méthodes de callback JPA
    @PreUpdate
    protected void onUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    // Méthodes métier
    public boolean isPublie() {
        return statut == Statut.PUBLIE;
    }

    public boolean isTermine() {
        return statut == Statut.TERMINE || LocalDateTime.now().isAfter(dateFin);
    }

    public boolean isAnnule() {
        return statut == Statut.ANNULE;
    }

    public boolean isDisponible() {
        return isPublie() && !isTermine() && !isAnnule();
    }

    public int getPlacesReservees() {
        if (reservations == null || !org.hibernate.Hibernate.isInitialized(reservations)) {
            return 0;
        }
        return reservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE ||
                        r.getStatut() == StatutReservation.EN_ATTENTE)
                .mapToInt(Reservation::getNombrePlaces)
                .sum();
    }

    public int getPlacesDisponibles() {
        if (capaciteMax == null) {
            return 0;
        }
        return capaciteMax - getPlacesReservees();
    }

    public double getTauxRemplissage() {
        return (double) getPlacesReservees() / capaciteMax * 100;
    }

    public boolean peutEtreModifie() {
        return statut == Statut.BROUILLON || statut == Statut.PUBLIE;
    }

    public boolean peutEtreSupprime() {
        return reservations.isEmpty() ||
                reservations.stream().allMatch(r -> r.getStatut() == StatutReservation.ANNULEE);
    }

    public boolean isComplet() {
        return getPlacesDisponibles() <= 0;
    }

    @AssertTrue(message = "La date de fin doit être après la date de début")
    private boolean isDateFinApresDebut() {
        return dateFin == null || dateDebut == null || dateFin.isAfter(dateDebut);
    }
}
