package com.event.event_reservation_system.service;

import com.event.event_reservation_system.Repositories.EventRepository;
import com.event.event_reservation_system.Repositories.ReservationRepository;
import com.event.event_reservation_system.Repositories.UserRepository;
import com.event.event_reservation_system.exception.BadRequestException;
import com.event.event_reservation_system.exception.ConflictException;
import com.event.event_reservation_system.exception.ResourceNotFoundException;
import com.event.event_reservation_system.modele.Role;
import com.event.event_reservation_system.modele.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional

public class UserService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Inscription d'un nouvel utilisateur
     */
    public User inscrire(User user) {
        log.info("Tentative d'inscription pour l'email: {}", user.getEmail());

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Un compte avec cet email existe déjà");
        }

        // Valider la force du mot de passe
        validerMotDePasse(user.getPassword());

        // Hasher le mot de passe
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Par défaut, nouveau utilisateur = CLIENT actif
        if (user.getRole() == null) {
            user.setRole(Role.CLIENT);
        }

        User savedUser = userRepository.save(user);
        log.info("Utilisateur inscrit avec succès: {}", savedUser.getEmail());

        return savedUser;
    }

    /**
     * Authentification d'un utilisateur
     */
    public Optional<User> authentifier(String email, String password) {
        log.info("Tentative d'authentification pour: {}", email);

        return userRepository.findByEmail(email)
                .filter(User::getActif)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()));
    }

    /**
     * Mise à jour du profil utilisateur
     */
    public User mettreAJourProfil(Long userId, User updatedInfo) {
        User user = trouverParId(userId);

        // Mise à jour des champs modifiables
        Optional.ofNullable(updatedInfo.getNom()).ifPresent(user::setNom);
        Optional.ofNullable(updatedInfo.getPrenom()).ifPresent(user::setPrenom);
        Optional.ofNullable(updatedInfo.getTelephone()).ifPresent(user::setTelephone);

        // Vérifier si l'email change et qu'il n'existe pas déjà
        if (updatedInfo.getEmail() != null && !updatedInfo.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updatedInfo.getEmail())) {
                throw new ConflictException("Cet email est déjà utilisé");
            }
            user.setEmail(updatedInfo.getEmail());
        }

        return userRepository.save(user);
    }

    /**
     * Changement de mot de passe
     */
    public void changerMotDePasse(Long userId, String ancienPassword, String nouveauPassword) {
        User user = trouverParId(userId);

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(ancienPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"L'ancien mot de passe est incorrect");
        }

        // Valider le nouveau mot de passe
        validerMotDePasse(nouveauPassword);

        // Changer le mot de passe
        user.setPassword(passwordEncoder.encode(nouveauPassword));
        userRepository.save(user);

        log.info("Mot de passe changé pour l'utilisateur: {}", user.getEmail());
    }

    /**
     * Désactivation d'un compte
     */
    public void desactiverCompte(Long userId) {
        User user = trouverParId(userId);
        user.setActif(false);
        userRepository.save(user);

        log.info("Compte désactivé: {}", user.getEmail());
    }

    /**
     * Activation d'un compte
     */
    public void activerCompte(Long userId) {
        User user = trouverParId(userId);
        user.setActif(true);
        userRepository.save(user);

        log.info("Compte activé: {}", user.getEmail());
    }

    /**
     * Changement de rôle (admin seulement)
     */
    public User changerRole(Long userId, Role nouveauRole) {
        User user = trouverParId(userId);
        user.setRole(nouveauRole);
        return userRepository.save(user);
    }

    /**
     * Récupération des statistiques utilisateur
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesUtilisateur(Long userId) {
        User user = trouverParId(userId);
        Map<String, Object> stats = new HashMap<>();

        // Nombre d'événements créés (pour organisateurs)
        long nbEvenementsCreés = eventRepository.findByOrganisateur(user).size();

        // Nombre de réservations
        long nbReservations = reservationRepository.findByUtilisateur(user).size();

        // Montant total dépensé
        BigDecimal montantDepense = reservationRepository.calculateTotalSpentByUser(user);

        stats.put("nombreEvenementsCreés", nbEvenementsCreés);
        stats.put("nombreReservations", nbReservations);
        stats.put("montantTotalDepense", montantDepense != null ? montantDepense : BigDecimal.ZERO);
        stats.put("role", user.getRole().getLabel());

        return stats;
    }

    /**
     * Liste des utilisateurs avec filtres
     */
    @Transactional(readOnly = true)
    public List<User> listerAvecFiltres(Boolean actif, Role role, String keyword) {
        return userRepository.searchWithFilters(actif, role, keyword);
    }

    /**
     * Trouver un utilisateur par ID
     */
    @Transactional(readOnly = true)
    public User trouverParId(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
    }

    /**
     * Trouver un utilisateur par email
     */
    @Transactional(readOnly = true)
    public Optional<User> trouverParEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Statistiques globales des utilisateurs
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getStatistiquesGlobales() {
        return Arrays.stream(Role.values())
                .collect(Collectors.toMap(
                        Role::getLabel,
                        userRepository::countByRole
                ));
    }

    /**
     * Validation de la force du mot de passe
     */
    private void validerMotDePasse(String password) {
        if (password == null || password.length() < 8) {
            throw new BadRequestException("Le mot de passe doit contenir au moins 8 caractères");
        }

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasUpper || !hasLower || !hasDigit) {
            throw new BadRequestException(
                    "Le mot de passe doit contenir au moins une majuscule, " +
                            "une minuscule et un chiffre"
            );
        }
    }
    /**
     * Lister tous les utilisateurs
     */
    @Transactional(readOnly = true)
    public List<User> listerTous() {
        return userRepository.findAll();
    }
    /**
     * Compter tous les utilisateurs
     */
    @Transactional(readOnly = true)
    public long compterTous() {
        return userRepository.count();
    }
    @Transactional(readOnly = true)
    public Map<String, Long> getUsersByRoleCount() {
        return java.util.Arrays.stream(Role.values())
                .collect(java.util.stream.Collectors.toMap(
                        Role::name,
                        userRepository::countByRole
                ));
    }
}
