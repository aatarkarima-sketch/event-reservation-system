package com.event.event_reservation_system.Repositories;

import com.event.event_reservation_system.dto.ReservationDashboardDTO;
import com.event.event_reservation_system.modele.Event;
import com.event.event_reservation_system.modele.Reservation;
import com.event.event_reservation_system.modele.StatutReservation;
import com.event.event_reservation_system.modele.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    /**
     * Trouve les réservations d'un utilisateur
     */
    List<Reservation> findByUtilisateur(User utilisateur);

    /**
     * Trouve les réservations d'un utilisateur triées par date
     */
    List<Reservation> findByUtilisateurOrderByDateReservationDesc(User utilisateur);

    /**
     * Trouve les réservations d'un événement avec un statut donné
     */
    List<Reservation> findByEvenementAndStatut(Event evenement, StatutReservation statut);

    /**
     * Trouve toutes les réservations d'un événement
     */
    List<Reservation> findByEvenement(Event evenement);

    /**
     * Calcule le nombre total de places réservées pour un événement
     */
    @Query("SELECT COALESCE(SUM(r.nombrePlaces), 0) FROM Reservation r " +
            "WHERE r.evenement = :evenement AND " +
            "(r.statut = 'CONFIRMEE' OR r.statut = 'EN_ATTENTE')")
    int countPlacesReserveesForEvent(@Param("evenement") Event evenement);

    /**
     * Trouve une réservation par code
     */
    Optional<Reservation> findByCodeReservation(String codeReservation);

    /**
     * Vérifie si un code de réservation existe
     */
    boolean existsByCodeReservation(String codeReservation);

    /**
     * Trouve les réservations entre deux dates
     */
    @Query("SELECT r FROM Reservation r WHERE " +
            "r.dateReservation BETWEEN :debut AND :fin")
    List<Reservation> findBetweenDates(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin
    );

    /**
     * Trouve les réservations confirmées d'un utilisateur
     */
    List<Reservation> findByUtilisateurAndStatut(User utilisateur, StatutReservation statut);


    /**
     * Compte les réservations par statut
     */
    long countByStatut(StatutReservation statut);

    /**
     * Trouve les réservations à venir pour un utilisateur
     */
    @Query("SELECT r FROM Reservation r WHERE r.utilisateur = :utilisateur AND " +
            "r.evenement.dateDebut > :now AND " +
            "(r.statut = 'CONFIRMEE' OR r.statut = 'EN_ATTENTE') " +
            "ORDER BY r.evenement.dateDebut ASC")
    List<Reservation> findUpcomingReservations(
            @Param("utilisateur") User utilisateur,
            @Param("now") LocalDateTime now
    );


    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.evenement.organisateur = :organisateur")
    long countByOrganisateur(@Param("organisateur") User organisateur);

    /**
     * Recherche de réservations avec filtres
     */
    @Query("SELECT r FROM Reservation r WHERE " +
            "(:utilisateur IS NULL OR r.utilisateur = :utilisateur) AND " +
            "(:evenement IS NULL OR r.evenement = :evenement) AND " +
            "(:statut IS NULL OR r.statut = :statut) AND " +
            "(:codeReservation IS NULL OR r.codeReservation LIKE CONCAT('%', :codeReservation, '%'))")
    List<Reservation> searchWithFilters(
            @Param("utilisateur") User utilisateur,
            @Param("evenement") Event evenement,
            @Param("statut") StatutReservation statut,
            @Param("codeReservation") String codeReservation
    );


    /**
     * Calcule le montant total de toutes les réservations confirmées
     */
    // Remplace les 3 méthodes suivantes :
    @Query("SELECT COALESCE(SUM(r.montantTotal), 0) FROM Reservation r WHERE r.statut = 'CONFIRMEE'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COALESCE(SUM(r.montantTotal), 0) FROM Reservation r " +
            "WHERE r.evenement.organisateur = :organisateur AND r.statut = 'CONFIRMEE'")
    BigDecimal calculateTotalRevenueForOrganizer(@Param("organisateur") User organisateur);

    @Query("SELECT COALESCE(SUM(r.montantTotal), 0) FROM Reservation r " +
            "WHERE r.utilisateur = :utilisateur AND r.statut = 'CONFIRMEE'")
    BigDecimal calculateTotalSpentByUser(@Param("utilisateur") User utilisateur);

    @Query("""
                SELECT new com.event.event_reservation_system.dto.ReservationDashboardDTO(
                    r.id,
                    r.codeReservation,
                    e.titre,
                    e.dateDebut,
                    r.nombrePlaces,
                    r.montantTotal,
                    r.statut
                )
                FROM Reservation r
                JOIN r.evenement e
                WHERE r.utilisateur = :user
                ORDER BY r.dateReservation DESC
            """)
    List<ReservationDashboardDTO> findDashboardReservations(
            @Param("user") User user
    );

    /**
     * Trouve les réservations à venir avec événements chargés (FETCH JOIN)
     */
    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.evenement e " +
            "WHERE r.utilisateur = :utilisateur AND " +
            "e.dateDebut > :now AND " +
            "(r.statut = 'CONFIRMEE' OR r.statut = 'EN_ATTENTE') " +
            "ORDER BY e.dateDebut ASC")
    List<Reservation> findUpcomingReservationsWithEvent(
            @Param("utilisateur") User utilisateur,
            @Param("now") LocalDateTime now
    );

    /**
     * Trouve toutes les réservations d'un utilisateur avec événements chargés
     */
    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.evenement e " +
            "WHERE r.utilisateur = :utilisateur " +
            "ORDER BY r.dateReservation DESC")
    List<Reservation> findByUtilisateurWithEvent(@Param("utilisateur") User utilisateur);


    @Query("SELECT r FROM Reservation r WHERE r.dateReservation >= :date ORDER BY r.dateReservation DESC")
    List<Reservation> findByDateCreationAfter(@Param("date") LocalDateTime date);


    @Query("SELECT COALESCE(SUM(r.montantTotal), 0) FROM Reservation r " +
            "WHERE r.dateReservation >= :date AND r.statut = 'CONFIRMEE'")
    BigDecimal calculateRevenueSince(@Param("date") LocalDateTime date);

    @Query("SELECT r FROM Reservation r ORDER BY r.dateReservation DESC")
    List<Reservation> findRecentReservations(Pageable pageable);
    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.evenement e " +
            "ORDER BY r.dateReservation DESC")
    List<Reservation> findRecentReservationsWithEvent(Pageable pageable);
    @Query("SELECT MONTH(r.dateReservation) AS mois, COUNT(r) AS count " +
            "FROM Reservation r " +
            "WHERE YEAR(r.dateReservation) = :year AND r.statut = 'CONFIRMEE' " +
            "GROUP BY MONTH(r.dateReservation) " +
            "ORDER BY MONTH(r.dateReservation)")
    List<Object[]> countReservationsByMonthYear(@Param("year") int year);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.utilisateur")
    List<Reservation> findAllWithUtilisateur();
    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.utilisateur " +
            "JOIN FETCH r.evenement")
    List<Reservation> findAllWithUtilisateurAndEvenement();
}
