package com.event.event_reservation_system.views.organizer;

import com.event.event_reservation_system.modele.Statut;
import com.event.event_reservation_system.modele.User;
import com.event.event_reservation_system.security.SecurityUtils;
import com.event.event_reservation_system.service.EventService;
import com.event.event_reservation_system.views.MainLayout;
import com.event.event_reservation_system.views.UnifiedLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Map;

@Route(value = "organizer/dashboard", layout = UnifiedLayout.class)
@PageTitle("Dashboard Organisateur | EventManager")
@RolesAllowed({"ORGANIZER", "ADMIN"})
@CssImport("./styles/organizer-dashboard.css")
public class OrganizerDashboardView extends VerticalLayout {

    private final EventService eventService;
    private final SecurityUtils securityUtils;

    public OrganizerDashboardView(EventService eventService, SecurityUtils securityUtils) {
        this.eventService = eventService;
        this.securityUtils = securityUtils;

        setSizeFull();
        addClassName("organizer-dashboard");

        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Utilisateur non connecté"));

        add(
                createQuickActions(),
                createStatsSection(currentUser),
                createRecentEventsSection()
        );
    }



    private Component createQuickActions() {
        Div container = new Div();
        container.addClassName("quick-actions");

        Button createEvent = new Button("Créer un événement", VaadinIcon.PLUS_CIRCLE.create());
        createEvent.addClassNames("action-btn", "primary");
        createEvent.addClickListener(e -> createEvent.getUI().ifPresent(ui -> ui.navigate("organizer/event/new")));

        Button myEvents = new Button("Mes événements", VaadinIcon.CALENDAR_BRIEFCASE.create());
        myEvents.addClassNames("action-btn", "secondary");
        myEvents.addClickListener(e -> myEvents.getUI().ifPresent(ui -> ui.navigate("organizer/events")));

        container.add(createEvent, myEvents);
        return container;
    }

    private Component createStatsSection(User user) {
        Map<String, Object> stats = eventService.getStatistiquesOrganisateur(user.getId());
        @SuppressWarnings("unchecked")
        Map<Statut, Long> eventsByStatus = (Map<Statut, Long>) stats.get("nombreEvenementsParStatut");

        Div grid = new Div();
        grid.addClassName("stats-grid");

        grid.add(
                createStatCard("Événements créés", stats.get("nombreEvenements").toString(),
                        VaadinIcon.CALENDAR.create(), "#26658C"),
                createStatCard("Événements publiés", eventsByStatus.getOrDefault(Statut.PUBLIE, 0L).toString(),
                        VaadinIcon.CHECK_CIRCLE.create(), "#54ACBF"),
                createStatCard("Réservations totales", stats.get("nombreReservations").toString(),
                        VaadinIcon.TICKET.create(), "#7DD3FC"),
                createStatCard("Revenu généré", String.format("%.0f DH", stats.get("revenuTotal")),
                        VaadinIcon.MONEY.create(), "#10B981")  // Vert pour l'argent, plus impactant
        );

        return grid;
    }

    // Méthode helper (ajustée pour accepter la couleur)
    private Component createStatCard(String label, String value, Icon icon, String accentColor) {
        Div card = new Div();
        card.addClassName("stat-card");
        card.getElement().getStyle().set("--accent-color", accentColor);

        Div iconWrapper = new Div(icon);
        iconWrapper.addClassName("stat-icon-wrapper");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("stat-value");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("stat-label");

        card.add(iconWrapper, valueSpan, labelSpan);
        return card;
    }


    private Component createRecentEventsSection() {
        Div section = new Div();
        section.addClassName("recent-events-section");

        H2 title = new H2("Événements récents");
        title.addClassName("section-title");

        Paragraph info = new Paragraph("Consultez et gérez tous vos événements publiés ou en brouillon.");
        info.addClassName("section-info");

        Button viewAll = new Button("Voir tous mes événements", VaadinIcon.ARROW_RIGHT.create());
        viewAll.addClassName("view-all-btn");
        viewAll.addClickListener(e -> viewAll.getUI().ifPresent(ui -> ui.navigate("organizer/events")));

        section.add(title, info, viewAll);
        return section;
    }
}
