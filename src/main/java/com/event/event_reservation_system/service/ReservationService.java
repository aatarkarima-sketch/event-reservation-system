package com.event.event_reservation_system.service;

import com.event.event_reservation_system.Repositories.EventRepository;
import com.event.event_reservation_system.Repositories.ReservationRepository;
import com.event.event_reservation_system.Repositories.UserRepository;
import com.event.event_reservation_system.dto.ReservationDashboardDTO;
import com.event.event_reservation_system.exception.BadRequestException;
import com.event.event_reservation_system.exception.BusinessException;
import com.event.event_reservation_system.exception.ForbiddenException;
import com.event.event_reservation_system.exception.ResourceNotFoundException;
import com.event.event_reservation_system.modele.Event;
import com.event.event_reservation_system.modele.Reservation;
import com.event.event_reservation_system.modele.StatutReservation;
import com.event.event_reservation_system.modele.User;
import com.event.event_reservation_system.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CodeGenerator codeGenerator;

    private static final int MAX_PLACES_PAR_RESERVATION = 10;

    /**
     * Création d'une réservation
     */
    public Reservation creerReservation(
            Long userId,
            Long eventId,
            Integer nombrePlaces,
            String commentaire
    ) {
        log.info("Création d'une réservation: User {} - Event {} - Places {}",
                userId, eventId, nombrePlaces);

        // Récupération et validation
        User utilisateur = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        Event evenement = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement", eventId));

        // Validations métier
        validerReservation(evenement, nombrePlaces);

        // Vérifier la disponibilité des places
        int placesDisponibles = evenement.getPlacesDisponibles();
        if (nombrePlaces > placesDisponibles) {
            throw new BusinessException(
                    String.format("Seulement %d places disponibles", placesDisponibles)
            );
        }

        // Générer le code unique
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (reservationRepository.existsByCodeReservation(code));


        // Créer la réservation
        Reservation reservation = Reservation.builder()
                .utilisateur(utilisateur)
                .evenement(evenement)
                .nombrePlaces(nombrePlaces)
                .codeReservation(code)
                .commentaire(commentaire)
                .statut(StatutReservation.EN_ATTENTE)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        log.info("Réservation créée avec succès: {}", saved.getCodeReservation());

        return saved;
    }

    /**
     * Confirmation d'une réservation
     */
    public Reservation confirmerReservation(Long reservationId) {
        Reservation reservation = trouverParId(reservationId);

        if (reservation.getStatut() == StatutReservation.CONFIRMEE) {
            throw new BusinessException("Cette réservation est déjà confirmée");
        }

        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new BusinessException("Une réservation annulée ne peut pas être confirmée");
        }

        reservation.setStatut(StatutReservation.CONFIRMEE);
        log.info("Réservation confirmée: {}", reservation.getCodeReservation());

        return reservationRepository.save(reservation);
    }

    /**
     * Annulation d'une réservation
     */
    public Reservation annulerReservation(Long reservationId, Long userId) {
        Reservation reservation = trouverParId(reservationId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        // Vérifier les permissions
        if (!peutAnnuler(reservation, user)) {
            throw new ForbiddenException("Vous n'avez pas les droits pour annuler cette réservation");
        }

        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new BusinessException("Cette réservation est déjà annulée");
        }

        // Vérifier le délai de 48h
        if (!reservation.peutEtreAnnulee()) {
            throw new BusinessException(
                    "Les réservations ne peuvent être annulées que jusqu'à 48h avant l'événement"
            );
        }

        reservation.setStatut(StatutReservation.ANNULEE);
        log.info("Réservation annulée: {}", reservation.getCodeReservation());

        return reservationRepository.save(reservation);
    }

    /**
     * Récupération des réservations d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsUtilisateur(Long userId) {
        User utilisateur = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        return reservationRepository.findByUtilisateurWithEvent(utilisateur);
    }

    /**
     * Récupération des réservations à venir pour un utilisateur
     */
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsAVenir(Long userId) {
        User utilisateur = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        return reservationRepository.findUpcomingReservationsWithEvent(utilisateur, LocalDateTime.now());
    }

    /**
     * Vérification d'une réservation par code
     */
    @Transactional(readOnly = true)
    public Optional<Reservation> verifierReservation(String code) {
        return reservationRepository.findByCodeReservation(code);
    }

    /**
     * Récapitulatif de réservation
     */
    @Transactional(readOnly = true)
    public String getRecapitulatif(Long reservationId) {
        Reservation reservation = trouverParId(reservationId);
        return reservation.getRecapitulatif();
    }

    /**
     * Réservations d'un événement
     */
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsEvenement(Long eventId) {
        Event evenement = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement", eventId));

        return reservationRepository.findByEvenement(evenement);
    }

    /**
     * Réservations d'un événement par statut
     */
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsEvenementParStatut(
            Long eventId,
            StatutReservation statut
    ) {
        Event evenement = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement", eventId));

        return reservationRepository.findByEvenementAndStatut(evenement, statut);
    }

    /**
     * Statistiques des réservations pour un événement
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesEvenement(Long eventId) {
        Event evenement = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement", eventId));

        List<Reservation> reservations = reservationRepository.findByEvenement(evenement);

        Map<String, Object> stats = new HashMap<>();
        stats.put("nombreReservations", reservations.size());
        stats.put("placesReservees", evenement.getPlacesReservees());
        stats.put("placesDisponibles", evenement.getPlacesDisponibles());
        stats.put("tauxRemplissage", evenement.getTauxRemplissage());

        Double revenu = reservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                .mapToDouble(Reservation::getMontantTotal)
                .sum();
        stats.put("revenu", revenu);

        Map<StatutReservation, Long> parStatut = reservations.stream()
                .collect(Collectors.groupingBy(Reservation::getStatut, Collectors.counting()));
        stats.put("parStatut", parStatut);

        return stats;
    }

    /**
     * Statistiques globales des réservations
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesGlobales() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("nombreTotal", reservationRepository.count());
        stats.put("revenuTotal", reservationRepository.calculateTotalRevenue());

        Arrays.stream(StatutReservation.values()).forEach(statut ->
                stats.put("nombre" + statut.name(), reservationRepository.countByStatut(statut))
        );

        return stats;
    }

    /**
     * Recherche de réservations avec filtres
     */
    @Transactional(readOnly = true)
    public List<Reservation> rechercherReservations(
            Long userId,
            Long eventId,
            StatutReservation statut,
            String codeReservation
    ) {
        User utilisateur = userId != null ?
                userRepository.findById(userId).orElse(null) : null;

        Event evenement = eventId != null ?
                eventRepository.findById(eventId).orElse(null) : null;

        return reservationRepository.searchWithFilters(
                utilisateur, evenement, statut, codeReservation
        );
    }

    /**
     * Trouver une réservation par ID
     */
    @Transactional(readOnly = true)
    public Reservation trouverParId(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation", id));
    }

    // Méthodes privées

    private void validerReservation(Event evenement, Integer nombrePlaces) {
        // Vérifier que l'événement est disponible
        if (!evenement.isDisponible()) {
            throw new BusinessException("Cet événement n'est pas disponible pour la réservation");
        }

        // Vérifier le nombre de places
        if (nombrePlaces == null || nombrePlaces < 1) {
            throw new BadRequestException("Le nombre de places doit être au moins 1");
        }

        if (nombrePlaces > MAX_PLACES_PAR_RESERVATION) {
            throw new BadRequestException(
                    String.format("Vous ne pouvez pas réserver plus de %d places", MAX_PLACES_PAR_RESERVATION)
            );
        }// Vérifier que l'événement n'est pas complet
        if (evenement.isComplet()) {
            throw new BusinessException("Cet événement est complet");
        }
    }

    private boolean peutAnnuler(Reservation reservation, User user) {
        // L'utilisateur peut annuler sa propre réservation
        if (reservation.getUtilisateur().getId().equals(user.getId())) {
            return true;
        }

        // L'organisateur de l'événement peut annuler
        if (reservation.getEvenement().getOrganisateur().getId().equals(user.getId())) {
            return true;
        }

        // L'admin peut tout annuler
        return user.isAdmin();
    }
    /**
     * Lister toutes les réservations
     */
    @Transactional(readOnly = true)
    public List<Reservation> listerToutes() {
        return reservationRepository.findAll();
    }
    @Transactional(readOnly = true)
    public long countAllConfirmed() {
        return reservationRepository.countByStatut(StatutReservation.CONFIRMEE);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue() {
        return reservationRepository.calculateTotalRevenue(); // à implémenter dans le repo
    }

    @Transactional(readOnly = true)
    public List<Reservation> getRecentReservations(int limit) {
        Pageable pageable = PageRequest.of(0, Math.min(limit, 50)); // sécurité max 50
        return reservationRepository.findRecentReservationsWithEvent(pageable);
    }

    @Transactional(readOnly = true)
    public double calculateConversionRate() {
        // Exemple simple : réservations confirmées / total réservations
        long confirmed = reservationRepository.countByStatut(StatutReservation.CONFIRMEE);
        long total = reservationRepository.count();
        return total > 0 ? (confirmed * 100.0 / total) : 0;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateMonthlyRevenue() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        return reservationRepository.calculateRevenueSince(startOfMonth);
    }
    @Transactional(readOnly = true)
    public Map<String, Long> getReservationsParMois() {
        int year = LocalDateTime.now().getYear();
        List<Object[]> results = reservationRepository.countReservationsByMonthYear(year);
        Map<String, Long> map = new LinkedHashMap<>();
        String[] mois = {"", "Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sept", "Oct", "Nov", "Déc"};
        for (int i = 1; i <= 12; i++) {
            map.put(mois[i], 0L);
        }
        for (Object[] row : results) {
            int month = (Integer) row[0];
            long count = (Long) row[1];
            map.put(mois[month], count);
        }
        return map;
    }
    @Transactional(readOnly = true)
    public List<Reservation> listerToutesAvecUtilisateur() {
        return reservationRepository.findAllWithUtilisateur();
    }
    @Transactional(readOnly = true)
    public List<Reservation> listerToutesAvecUtilisateurEtEvenement() {
        return reservationRepository.findAllWithUtilisateurAndEvenement();
    }
}
