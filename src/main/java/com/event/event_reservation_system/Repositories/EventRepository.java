package com.event.event_reservation_system.Repositories;

import com.event.event_reservation_system.modele.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Recherche dynamique avec filtres
     */
    @Query("""
        SELECT e FROM Event e
        WHERE (:categorie IS NULL OR e.categorie = :categorie)
          AND (:statut IS NULL OR e.statut = :statut)
          AND (:ville IS NULL OR LOWER(e.ville) = LOWER(:ville))
          AND (:minPrix IS NULL OR e.prixUnitaire >= :minPrix)
          AND (:maxPrix IS NULL OR e.prixUnitaire <= :maxPrix)
          AND (:dateDebut IS NULL OR e.dateDebut >= :dateDebut)
          AND (:dateFin IS NULL OR e.dateFin <= :dateFin)
          AND (:keyword IS NULL OR LOWER(e.titre) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    List<Event> searchWithFilters(
            @Param("categorie") Categorie categorie,
            @Param("statut") Statut statut,
            @Param("ville") String ville,
            @Param("minPrix") Double minPrix,
            @Param("maxPrix") Double maxPrix,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin,
            @Param("keyword") String keyword
    );

    /**
     * Trouver les événements encore disponibles
     */
    @Query("""
        SELECT e FROM Event e
        WHERE e.dateFin > :now
          AND e.statut = com.event.event_reservation_system.modele.Statut.PUBLIE
    """)
    List<Event> findAvailableEvents(@Param("now") LocalDateTime now);

    /**
     * Événements populaires (triés par nombre de réservations)
     */
    @Query("""
        SELECT e FROM Event e
        LEFT JOIN e.reservations r
        GROUP BY e
        ORDER BY COUNT(r) DESC
    """)
    List<Event> findMostPopular();

    /**
     * Événements à marquer comme terminés
     */
    @Query("""
        SELECT e FROM Event e
        WHERE e.dateFin < :now
          AND e.statut = com.event.event_reservation_system.modele.Statut.PUBLIE
    """)
    List<Event> findEventsToMarkAsFinished(@Param("now") LocalDateTime now);

    /**
     * Événements d'un organisateur
     */
    List<Event> findByOrganisateur(User organisateur);

    /**
     * Liste de toutes les villes uniques
     */
    @Query("SELECT DISTINCT e.ville FROM Event e")
    List<String> findAllVilles();

    long countByStatut(Statut statut);

    List<Event> findByCategorieAndStatut(Categorie categorie, Statut statut);

    @Query("SELECT e FROM Event e JOIN FETCH e.organisateur")
    List<Event> findAllWithOrganisateur();
}

