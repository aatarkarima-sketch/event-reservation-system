package com.event.event_reservation_system.Repositories;

import com.event.event_reservation_system.modele.Role;
import com.event.event_reservation_system.modele.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Trouve un utilisateur par email
     */
    Optional<User> findByEmail(String email);

    /**
     * Trouve tous les utilisateurs actifs avec un rôle spécifique
     */
    List<User> findByActifTrueAndRole(Role role);

    /**
     * Vérifie si un email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Recherche par nom ou prénom (insensible à la casse)
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByNomOrPrenom(@Param("keyword") String keyword);

    /**
     * Compte le nombre d'utilisateurs par rôle
     */
    long countByRole(Role role);

    /**
     * Trouve tous les utilisateurs par rôle
     */
    List<User> findByRole(Role role);

    /**
     * Trouve tous les utilisateurs actifs
     */
    List<User> findByActifTrue();

    /**
     * Compte tous les utilisateurs actifs
     */
    long countByActifTrue();

    /**
     * Recherche avancée avec plusieurs critères
     */
    @Query("SELECT u FROM User u WHERE " +
            "(:actif IS NULL OR u.actif = :actif) AND " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:keyword IS NULL OR LOWER(u.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<User> searchWithFilters(
            @Param("actif") Boolean actif,
            @Param("role") Role role,
            @Param("keyword") String keyword
    );
}
