package com.event.event_reservation_system.dto;

import com.event.event_reservation_system.modele.StatutReservation;

import java.time.LocalDateTime;

public record ReservationDashboardDTO(
        Long reservationId,
        String code,
        String eventTitre,
        LocalDateTime dateEvent,
        Integer places,
        Double montant,
        StatutReservation statut
) {}

