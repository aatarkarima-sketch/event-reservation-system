package com.event.event_reservation_system.service;

import com.event.event_reservation_system.Repositories.EventRepository;
import com.event.event_reservation_system.Repositories.ReservationRepository;
import com.event.event_reservation_system.Repositories.UserRepository;
import com.event.event_reservation_system.dto.EventDTO;
import com.event.event_reservation_system.exception.BadRequestException;
import com.event.event_reservation_system.exception.BusinessException;
import com.event.event_reservation_system.exception.ForbiddenException;
import com.event.event_reservation_system.exception.ResourceNotFoundException;
import com.event.event_reservation_system.modele.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    /**
     * Création d'un événement (ADMIN ou ORGANIZER)
     */
    public Event creerEvenement(Event event, Long organisateurId) {
        log.info("Création d'un événement par l'utilisateur ID: {}", organisateurId);

        User organisateur = userRepository.findById(organisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", organisateurId));

        // Vérifier les permissions
        if (!organisateur.isOrganizer()) {
            throw new ForbiddenException("Seuls les organisateurs peuvent créer des événements");
        }

        // Valider les dates
        validerDates(event.getDateDebut(), event.getDateFin());

        event.setOrganisateur(organisateur);
        event.setStatut(Statut.BROUILLON);

        Event saved = eventRepository.save(event);
        log.info("Événement créé avec succès: ID {}", saved.getId());

        return saved;
    }

    /**
     * Modification d'un événement
     */
    public Event modifierEvenement(Long eventId, Event updatedEvent, Long userId) {
        Event event = trouverParId(eventId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        // Vérifier les permissions
        if (!peutModifier(event, user)) {
            throw new ForbiddenException("Vous n'avez pas les droits pour modifier cet événement");
        }

        // Vérifier si l'événement peut être modifié
        if (!event.peutEtreModifie()) {
            throw new BusinessException("Cet événement ne peut plus être modifié");
        }

        // Mise à jour des champs
        Optional.ofNullable(updatedEvent.getTitre()).ifPresent(event::setTitre);
        Optional.ofNullable(updatedEvent.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(updatedEvent.getCategorie()).ifPresent(event::setCategorie);
        Optional.ofNullable(updatedEvent.getLieu()).ifPresent(event::setLieu);
        Optional.ofNullable(updatedEvent.getVille()).ifPresent(event::setVille);
        Optional.ofNullable(updatedEvent.getCapaciteMax()).ifPresent(event::setCapaciteMax);
        Optional.ofNullable(updatedEvent.getPrixUnitaire()).ifPresent(event::setPrixUnitaire);
        Optional.ofNullable(updatedEvent.getImageUrl()).ifPresent(event::setImageUrl);

        // Valider et mettre à jour les dates si modifiées
        if (updatedEvent.getDateDebut() != null || updatedEvent.getDateFin() != null) {
            LocalDateTime newDebut = updatedEvent.getDateDebut() != null ?
                    updatedEvent.getDateDebut() : event.getDateDebut();
            LocalDateTime newFin = updatedEvent.getDateFin() != null ?
                    updatedEvent.getDateFin() : event.getDateFin();

            validerDates(newDebut, newFin);
            event.setDateDebut(newDebut);
            event.setDateFin(newFin);
        }

        return eventRepository.save(event);
    }

    /**
     * Publication d'un événement
     */
    public Event publierEvenement(Long eventId, Long userId) {
        Event event = trouverParId(eventId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        if (!peutModifier(event, user)) {
            throw new ForbiddenException("Vous n'avez pas les droits pour publier cet événement");
        }

        if (event.getStatut() != Statut.BROUILLON) {
            throw new BusinessException("Seuls les événements en brouillon peuvent être publiés");
        }

        // Vérifier que toutes les informations requises sont présentes
        if (!estComplet(event)) {
            throw new BusinessException("L'événement doit avoir toutes les informations requises avant publication");
        }

        event.setStatut(Statut.PUBLIE);
        log.info("Événement publié: ID {}", eventId);

        return eventRepository.save(event);
    }

    /**
     * Annulation d'un événement
     */
    public Event annulerEvenement(Long eventId, Long userId) {
        Event event = trouverParId(eventId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        if (!peutModifier(event, user)) {
            throw new ForbiddenException("Vous n'avez pas les droits pour annuler cet événement");
        }

        if (event.isTermine()) {
            throw new BusinessException("Un événement terminé ne peut pas être annulé");
        }

        event.setStatut(Statut.ANNULE);

        // Annuler toutes les réservations associées
        event.getReservations().stream()
                .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
                .forEach(r -> r.setStatut(StatutReservation.ANNULEE));

        log.info("Événement annulé: ID {}", eventId);

        return eventRepository.save(event);
    }

    /**
     * Suppression d'un événement
     */
    public void supprimerEvenement(Long eventId, Long userId) {
        Event event = trouverParId(eventId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        if (!peutModifier(event, user)) {
            throw new ForbiddenException("Vous n'avez pas les droits pour supprimer cet événement");
        }

        if (!event.peutEtreSupprime()) {
            throw new BusinessException("Cet événement a des réservations actives et ne peut pas être supprimé");
        }

        eventRepository.delete(event);
        log.info("Événement supprimé: ID {}", eventId);
    }

    /**
     * Recherche d'événements avec filtres
     */
    @Transactional(readOnly = true)
    public List<Event> rechercherEvenements(
            Categorie categorie,
            Statut statut,
            String ville,
            Double minPrix,
            Double maxPrix,
            LocalDateTime dateDebut,
            LocalDateTime dateFin,
            String keyword
    ) {
        return eventRepository.searchWithFilters(
                categorie, statut, ville, minPrix, maxPrix, dateDebut, dateFin, keyword
        );
    }

    /**
     * Récupération des événements disponibles avec DTO
     */
    @Transactional(readOnly = true)
    public List<EventDTO> getEvenementsDisponiblesDTO() {
        List<Event> events = eventRepository.findAvailableEvents(LocalDateTime.now());

        return events.stream()
                .map(event -> {
                    int placesReservees = reservationRepository.countPlacesReserveesForEvent(event);
                    return EventDTO.fromEntity(event, placesReservees);
                })
                .collect(Collectors.toList());
    }

    /**
     * Recherche d'événements avec DTO
     */
    @Transactional(readOnly = true)
    public List<EventDTO> rechercherEvenementsDTO(
            Categorie categorie,
            Statut statut,
            String ville,
            Double minPrix,
            Double maxPrix,
            LocalDateTime dateDebut,
            LocalDateTime dateFin,
            String keyword
    ) {
        List<Event> events = eventRepository.searchWithFilters(
                categorie, statut, ville, minPrix, maxPrix, dateDebut, dateFin, keyword
        );

        return events.stream()
                .map(event -> {
                    int placesReservees = reservationRepository.countPlacesReserveesForEvent(event);
                    return EventDTO.fromEntity(event, placesReservees);
                })
                .collect(Collectors.toList());
    }

    /**
     * Événements populaires avec DTO
     */
    @Transactional(readOnly = true)
    public List<EventDTO> getEvenementsPopulairesDTO(int limit) {
        List<Event> events = eventRepository.findMostPopular();

        return events.stream()
                .limit(limit)
                .map(event -> {
                    int placesReservees = reservationRepository.countPlacesReserveesForEvent(event);
                    return EventDTO.fromEntity(event, placesReservees);
                })
                .collect(Collectors.toList());
    }

    /**
     * Calcul des places disponibles
     */
    @Transactional(readOnly = true)
    public int getPlacesDisponibles(Long eventId) {
        Event event = trouverParId(eventId);
        return event.getPlacesDisponibles();
    }

    /**
     * Événements populaires
     */
    @Transactional(readOnly = true)
    public List<Event> getEvenementsPopulaires(int limit) {
        return eventRepository.findMostPopular().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Statistiques d'un organisateur
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesOrganisateur(Long organisateurId) {
        User organisateur = userRepository.findById(organisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", organisateurId));

        List<Event> events = eventRepository.findByOrganisateur(organisateur);

        Map<String, Object> stats = new HashMap<>();
        stats.put("nombreEvenements", events.size());
        stats.put("nombreEvenementsParStatut",
                events.stream().collect(Collectors.groupingBy(Event::getStatut, Collectors.counting()))
        );
        stats.put("nombreReservations", reservationRepository.countByOrganisateur(organisateur));
        stats.put("revenuTotal", reservationRepository.calculateTotalRevenueForOrganizer(organisateur));

        return stats;
    }

    /**
     * Vérification automatique des événements terminés
     */
    @Transactional
    public void verifierEvenementsTermines() {
        List<Event> eventsATerminer = eventRepository.findEventsToMarkAsFinished(LocalDateTime.now());

        eventsATerminer.forEach(event -> {
            event.setStatut(Statut.TERMINE);
            log.info("Événement marqué comme terminé: ID {}", event.getId());
        });

        if (!eventsATerminer.isEmpty()) {
            eventRepository.saveAll(eventsATerminer);
        }
    }

    /**
     * Trouver un événement par ID
     */
    @Transactional(readOnly = true)
    public Event trouverParId(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));

        // Force le chargement de l'organisateur
        if (event.getOrganisateur() != null) {
            event.getOrganisateur().getPrenom(); // Déclenche le chargement
        }

        return event;
    }

    /**
     * Obtenir toutes les villes
     */
    @Transactional(readOnly = true)
    public List<String> getToutesLesVilles() {
        return eventRepository.findAllVilles();
    }

    // Méthodes privées

    private boolean peutModifier(Event event, User user) {
        return user.isAdmin() || event.getOrganisateur().getId().equals(user.getId());
    }

    private void validerDates(LocalDateTime debut, LocalDateTime fin) {
        if (debut.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La date de début doit être dans le futur");
        }

        if (fin.isBefore(debut)) {
            throw new BadRequestException("La date de fin doit être après la date de début");
        }
    }

    private boolean estComplet(Event event) {
        return event.getTitre() != null &&
                event.getCategorie() != null &&
                event.getDateDebut() != null &&
                event.getDateFin() != null &&
                event.getLieu() != null &&
                event.getVille() != null &&
                event.getCapaciteMax() != null &&
                event.getPrixUnitaire() != null;
    }
    /**
     * Trouver les événements d'un organisateur
     */
    @Transactional(readOnly = true)
    public List<Event> trouverParOrganisateur(User organisateur) {
        return eventRepository.findByOrganisateur(organisateur);
    }
    /**
     * Compter tous les événements
     */
    @Transactional(readOnly = true)
    public long compterTous() {
        return eventRepository.count();
    }
    /**
     * Lister tous les événements
     */
    @Transactional(readOnly = true)
    public List<Event> listerTous() {
        return eventRepository.findAll();
    }
    /**
     * Compter les événements par statut
     */
    @Transactional(readOnly = true)
    public long compterParStatut(Statut statut) {
        return eventRepository.countByStatut(statut);
    }
    /**
     * Récupérer les événements par catégorie (utilisé pour le filtrage sur la page d'accueil)
     */
    @Transactional(readOnly = true)
    public List<EventDTO> getEvenementsByCategorie(String categorieLabel, int limit) {
        if (categorieLabel == null || "Tous".equalsIgnoreCase(categorieLabel)) {
            return getEvenementsPopulairesDTO(limit);
        }

        // Convertir le label (String) en enum Categorie
        Categorie categorie = Categorie.fromString(categorieLabel);

// Appeler la méthode corrigée du repository
        List<Event> events = eventRepository.findByCategorieAndStatut(categorie, Statut.PUBLIE);

        return events.stream()
                .limit(limit)
                .map(event -> {
                    int placesReservees = reservationRepository.countPlacesReserveesForEvent(event);
                    return EventDTO.fromEntity(event, placesReservees);
                })
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public int getPlacesReservees(Long eventId) {
        Event event = trouverParId(eventId);
        return reservationRepository.countPlacesReserveesForEvent(event);
    }
    @Transactional(readOnly = true)
    public Map<Statut, Long> getEventsCountByStatus() {
        return java.util.Arrays.stream(Statut.values())
                .collect(java.util.stream.Collectors.toMap(
                        statut -> statut,
                        eventRepository::countByStatut
                ));
    }

    @Transactional(readOnly = true)
    public List<Event> listerTousAvecOrganisateur() {
        return eventRepository.findAllWithOrganisateur();
    }
}