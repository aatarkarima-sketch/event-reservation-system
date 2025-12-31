package com.event.event_reservation_system.modele;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import com.event.event_reservation_system.modele.Event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    @Column(nullable = false)
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.CLIENT;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime dateInscription = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Format de téléphone invalide")
    private String telephone;

    // Relations
    @OneToMany(mappedBy = "organisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Event> evenementsOrganises = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Reservation> reservations = new ArrayList<>();

    // Méthodes utilitaires
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isOrganizer() {
        return role == Role.ORGANIZER || role == Role.ADMIN;
    }

    public boolean isClient() {
        return role == Role.CLIENT;
    }

}
