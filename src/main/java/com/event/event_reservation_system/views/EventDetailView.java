package com.event.event_reservation_system.views;

import com.event.event_reservation_system.modele.Event;
import com.event.event_reservation_system.security.SecurityUtils;
import com.event.event_reservation_system.service.EventService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Route(value = "details/:id", layout = UnifiedLayout.class)
@PageTitle("Détails de l'événement | Event Manager")
@CssImport("./styles/event-detail-view.css")
@AnonymousAllowed
public class EventDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final SecurityUtils securityUtils;
    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("HH:mm");

    private Event event;
    private Long eventId;

    public EventDetailView(EventService eventService, SecurityUtils securityUtils) {
        this.eventService = eventService;
        this.securityUtils = securityUtils;

        addClassName("event-detail-view");
        setPadding(false);
        setSpacing(false);
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        try {
            eventId = Long.parseLong(event.getRouteParameters().get("id").orElse("0"));
            this.event = eventService.trouverParId(eventId);
            createContent();
        } catch (Exception e) {

            Notification.show("Événement non trouvé", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            event.forwardTo("events");
        }
    }

    private void createContent() {
        removeAll();
        add(
                createBreadcrumb(),
                createHeroSection(),
                createMainContent()
        );
    }

    // ==================== BREADCRUMB ====================
    private Component createBreadcrumb() {
        Div breadcrumb = new Div();
        breadcrumb.addClassName("breadcrumb-container");

        Anchor home = new Anchor("", "Accueil");
        home.addClassName("breadcrumb-link");

        Span separator1 = new Span("›");
        separator1.addClassName("breadcrumb-separator");

        Anchor events = new Anchor("events", "Événements");
        events.addClassName("breadcrumb-link");

        Span separator2 = new Span("›");
        separator2.addClassName("breadcrumb-separator");

        Span current = new Span(event.getTitre());
        current.addClassName("breadcrumb-current");

        breadcrumb.add(home, separator1, events, separator2, current);
        return breadcrumb;
    }

    // ==================== HERO SECTION ====================
    private Component createHeroSection() {
        Div hero = new Div();
        hero.addClassName("hero-section");

        // Image
        Image heroImage = new Image(getEventImage(), event.getTitre());
        heroImage.addClassName("hero-image");

        // Overlay
        Div overlay = new Div();
        overlay.addClassName("hero-overlay");

        // Bouton retour
        Button backButton = new Button(VaadinIcon.ARROW_LEFT.create());
        backButton.addClassName("back-button");
        backButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("events"))
        );

        // Contenu du hero
        Div heroContent = new Div();
        heroContent.addClassName("hero-content");

        // Badges
        Div badges = new Div();
        badges.addClassName("hero-badges");

        Span categoryBadge = new Span(event.getCategorie().getDisplayName());
        categoryBadge.addClassName("hero-badge");
        categoryBadge.getStyle().set("background", event.getCategorie().getColor());

        Span statusBadge = new Span(event.getStatut().getLabel());
        statusBadge.addClassName("hero-badge");
        statusBadge.getStyle().set("background", event.getStatut().getColor());

        badges.add(categoryBadge, statusBadge);

        // Titre
        H1 title = new H1(event.getTitre());
        title.addClassName("hero-title");

        // Meta info
        Div metaInfo = new Div();
        metaInfo.addClassName("hero-meta");

        Span date = createMetaItem(VaadinIcon.CALENDAR,
                event.getDateDebut().format(dateFormatter));
        Span time = createMetaItem(VaadinIcon.CLOCK,
                event.getDateDebut().format(timeFormatter));
        Span location = createMetaItem(VaadinIcon.MAP_MARKER,
                event.getVille());

        metaInfo.add(date, time, location);

        heroContent.add(badges, title, metaInfo);
        hero.add(heroImage, overlay, backButton, heroContent);

        return hero;
    }

    private Span createMetaItem(VaadinIcon icon, String text) {
        Span item = new Span();
        item.addClassName("hero-meta-item");

        Icon iconComponent = icon.create();
        Span textSpan = new Span(text);

        item.add(iconComponent, textSpan);
        return item;
    }

    // ==================== MAIN CONTENT ====================
    private Component createMainContent() {
        Div wrapper = new Div();
        wrapper.addClassName("main-content-wrapper");

        // Colonne gauche
        Div leftColumn = new Div();
        leftColumn.addClassName("left-column");
        leftColumn.add(
                createQuickInfoCards(),
                createDescriptionSection(),
                createOrganizerSection()
        );

        // Colonne droite
        Div rightColumn = new Div();
        rightColumn.addClassName("right-column");
        rightColumn.add(createReservationCard());

        wrapper.add(leftColumn, rightColumn);
        return wrapper;
    }

    // ==================== QUICK INFO CARDS ====================
    private Component createQuickInfoCards() {
        Div grid = new Div();
        grid.addClassName("quick-info-cards");

        grid.add(
                createInfoCard(VaadinIcon.CALENDAR, "Date",
                        event.getDateDebut().format(dateFormatter)),
                createInfoCard(VaadinIcon.CLOCK, "Heure",
                        event.getDateDebut().format(timeFormatter) + " - " +
                                event.getDateFin().format(timeFormatter)),
                createInfoCard(VaadinIcon.MAP_MARKER, "Lieu",
                        event.getLieu()),
                createInfoCard(VaadinIcon.EURO, "Prix",
                        String.format("%.0f€", event.getPrixUnitaire()))
        );

        return grid;
    }

    private Div createInfoCard(VaadinIcon icon, String label, String value) {
        Div card = new Div();
        card.addClassName("info-card");

        Div iconDiv = new Div();
        iconDiv.addClassName("info-card-icon");
        iconDiv.add(icon.create());

        Span labelSpan = new Span(label);
        labelSpan.addClassName("info-card-label");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("info-card-value");

        card.add(iconDiv, labelSpan, valueSpan);
        return card;
    }

    // ==================== DESCRIPTION ====================
    private Component createDescriptionSection() {
        Div card = new Div();
        card.addClassName("description-card");

        H2 title = new H2("Description");
        title.addClassName("description-title");

        Paragraph description = new Paragraph(
                event.getDescription() != null && !event.getDescription().isEmpty() ?
                        event.getDescription() :
                        "Aucune description disponible pour cet événement."
        );
        description.addClassName("description-text");

        card.add(title, description);
        return card;
    }

    // ==================== ORGANIZER ====================
    private Component createOrganizerSection() {
        Div card = new Div();
        card.addClassName("organizer-card");

        H3 title = new H3("Organisateur");
        title.addClassName("organizer-title");

        Div content = new Div();
        content.addClassName("organizer-content");

        // Avatar
        Div avatar = new Div();
        avatar.addClassName("organizer-avatar");
        avatar.add(new Span(event.getOrganisateur().getPrenom().substring(0, 1).toUpperCase()));

        // Info
        Div info = new Div();
        info.addClassName("organizer-info");

        Div name = new Div();
        name.setText(event.getOrganisateur().getNomComplet());
        name.addClassName("organizer-name");

        Div email = new Div();
        email.addClassName("organizer-contact");
        email.add(VaadinIcon.ENVELOPE.create(), new Span(event.getOrganisateur().getEmail()));

        info.add(name, email);

        if (event.getOrganisateur().getTelephone() != null) {
            Div phone = new Div();
            phone.addClassName("organizer-contact");
            phone.add(VaadinIcon.PHONE.create(),
                    new Span(event.getOrganisateur().getTelephone()));
            info.add(phone);
        }

        content.add(avatar, info);
        card.add(title, content);

        return card;
    }

    // ==================== RESERVATION CARD ====================
    private Component createReservationCard() {
        Div card = new Div();
        card.addClassName("reservation-card");

        // Section Prix
        Div priceSection = new Div();
        priceSection.addClassName("price-section");

        Span priceLabel = new Span("Prix");
        priceLabel.addClassName("price-label");

        Span priceValue = new Span(String.format("%.0f€", event.getPrixUnitaire()));
        priceValue.addClassName("price-value");

        priceSection.add(priceLabel, priceValue);

        // Section Disponibilité
        Div availabilitySection = new Div();
        availabilitySection.addClassName("availability-section");

        Div availabilityTitle = new Div();
        availabilityTitle.addClassName("availability-title");
        availabilityTitle.add(VaadinIcon.TICKET.create(), new Span("Places disponibles"));

        // Barre de progression
        Div progressBar = new Div();
        progressBar.addClassName("availability-bar");

        Div progressFill = new Div();
        progressFill.addClassName("availability-fill");

        double tauxRemplissage = event.getTauxRemplissage();
        progressFill.getStyle().set("width", tauxRemplissage + "%");

        if (tauxRemplissage > 80) {
            progressFill.addClassName("warning");
        }

        progressBar.add(progressFill);

        // Stats
        Div stats = new Div();
        stats.addClassName("availability-stats");

        Span reserved = new Span(event.getPlacesReservees() + " réservées");
        reserved.addClassName("availability-reserved");

        Span remaining = new Span(event.getPlacesDisponibles() + " restantes");
        remaining.addClassName("availability-remaining");

        if (event.getPlacesDisponibles() <= 10) {
            remaining.addClassName("warning");
        }

        stats.add(reserved, remaining);

        availabilitySection.add(availabilityTitle, progressBar, stats);

        // Bouton de réservation
        Component reservationButton = createReservationButton();

        card.add(priceSection, availabilitySection, reservationButton);
        return card;
    }

    private Component createReservationButton() {
        if (!event.isDisponible()) {
            Div warning = new Div();
            warning.addClassName("warning-message");
            warning.addClassName("unavailable");
            warning.add(new Paragraph("❌ Événement non disponible"));
            return warning;
        }

        if (event.isComplet()) {
            Div warning = new Div();
            warning.addClassName("warning-message");
            warning.addClassName("full");
            warning.add(new Paragraph("😞 Complet"));
            return warning;
        }

        Div container = new Div();

        Button button = new Button("Réserver", VaadinIcon.TICKET.create());
        button.addClassName("reserve-button");
        button.addClickListener(e -> {
            if (securityUtils.isUserLoggedIn()) {
                getUI().ifPresent(ui -> ui.navigate("event/" + eventId + "/reserve"));
            } else {
                Notification.show("Connectez-vous pour réserver", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                getUI().ifPresent(ui -> ui.navigate("login"));
            }
        });

        Span notice = new Span("Vous devez être connecté pour réserver.");
        notice.addClassName("connection-notice");

        container.add(button, notice);
        return container;
    }

    // ==================== UTILS ====================
    private String getEventImage() {
        if (event.getImageUrl() != null && !event.getImageUrl().isBlank()) {
            return event.getImageUrl();
        }

        return switch (event.getCategorie().name()) {
            case "CONCERT" -> "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=1920&h=600&fit=crop";
            case "THEATRE" -> "https://images.unsplash.com/photo-1503095396549-807759245b35?w=1920&h=600&fit=crop";
            case "CONFERENCE" -> "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=1920&h=600&fit=crop";
            case "SPORT" -> "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=1920&h=600&fit=crop";
            default -> "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=1920&h=600&fit=crop";
        };
    }
}