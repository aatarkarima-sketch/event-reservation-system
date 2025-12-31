package com.event.event_reservation_system.views.organizer;

import com.event.event_reservation_system.modele.Event;
import com.event.event_reservation_system.modele.Reservation;
import com.event.event_reservation_system.modele.StatutReservation;
import com.event.event_reservation_system.service.EventService;
import com.event.event_reservation_system.service.ReservationService;
import com.event.event_reservation_system.views.UnifiedLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Route(value = "organizer/event/:id/reservations", layout = UnifiedLayout.class)
@PageTitle("Réservations | Event Manager")
@RolesAllowed({"ORGANIZER", "ADMIN"})
@CssImport("./styles/event-reservations.css")
public class EventReservationsView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final ReservationService reservationService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Event event;
    private Grid<Reservation> grid;
    private TextField searchField;
    private ComboBox<StatutReservation> statusFilter;
    private List<Reservation> allReservations;

    public EventReservationsView(EventService eventService, ReservationService reservationService) {
        this.eventService = eventService;
        this.reservationService = reservationService;

        addClassName("reservations-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        try {
            Long eventId = Long.parseLong(
                    beforeEnterEvent.getRouteParameters().get("id").orElse("0")
            );

            event = eventService.trouverParId(eventId);
            allReservations = reservationService.getReservationsEvenement(eventId);

            createContent();

        } catch (Exception e) {
            Notification.show(
                    "Événement non trouvé",
                    3000,
                    Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
            beforeEnterEvent.forwardTo("organizer/events");
        }
    }

    private void createContent() {
        removeAll();

        // Container principal avec padding
        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.addClassName("main-container");
        mainContainer.setSizeFull();
        mainContainer.setPadding(true);
        mainContainer.setSpacing(true);

        mainContainer.add(
                createTopBar(),
                createHeader(),
                createStatsSection(),
                createFiltersSection(),
                createGridSection()
        );

        add(mainContainer);
        updateGrid();
    }

    private Component createTopBar() {
        Button backButton = new Button("Retour aux événements", VaadinIcon.ARROW_LEFT.create());
        backButton.addClassName("back-button");
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("organizer/events"))
        );

        HorizontalLayout topBar = new HorizontalLayout(backButton);
        topBar.addClassName("top-bar");
        topBar.setWidthFull();
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        return topBar;
    }

    private Component createHeader() {
        H2 title = new H2("Gestion des Réservations");
        title.addClassName("page-title");

        H3 eventTitle = new H3(event.getTitre());
        eventTitle.addClassName("event-title");

        Div dateInfo = new Div();
        dateInfo.addClassName("info-badge");
        Span dateIcon = new Span("📅");
        Span dateText = new Span(event.getDateDebut().format(dateFormatter));
        dateInfo.add(dateIcon, dateText);

        Div locationInfo = new Div();
        locationInfo.addClassName("info-badge");
        Span locationIcon = new Span("📍");
        Span locationText = new Span(event.getLieu() + ", " + event.getVille());
        locationInfo.add(locationIcon, locationText);

        HorizontalLayout infoLayout = new HorizontalLayout(dateInfo, locationInfo);
        infoLayout.addClassName("info-layout");
        infoLayout.setSpacing(true);

        VerticalLayout header = new VerticalLayout(title, eventTitle, infoLayout);
        header.addClassName("header-section");
        header.setPadding(false);
        header.setSpacing(true);

        return header;
    }

    private Component createStatsSection() {
        Map<String, Object> stats = reservationService.getStatistiquesEvenement(event.getId());

        Div totalCard = createStatCard(
                "Total Réservations",
                stats.get("nombreReservations").toString(),
                "💼",
                "stat-card-blue"
        );

        Div placesCard = createStatCard(
                "Places Réservées",
                stats.get("placesReservees") + " / " + event.getCapaciteMax(),
                "🎟️",
                "stat-card-indigo"
        );

        Div tauxCard = createStatCard(
                "Taux de Remplissage",
                String.format("%.1f%%", stats.get("tauxRemplissage")),
                "📊",
                "stat-card-purple"
        );

        Div revenueCard = createStatCard(
                "Revenu Total",
                String.format("%.2f DH", stats.get("revenu")),
                "💰",
                "stat-card-cyan"
        );

        HorizontalLayout statsLayout = new HorizontalLayout(
                totalCard, placesCard, tauxCard, revenueCard
        );
        statsLayout.addClassName("stats-grid");
        statsLayout.setWidthFull();

        return statsLayout;
    }

    private Div createStatCard(String label, String value, String icon, String className) {
        Span iconSpan = new Span(icon);
        iconSpan.addClassName("stat-icon");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("stat-value");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("stat-label");

        VerticalLayout content = new VerticalLayout(iconSpan, valueSpan, labelSpan);
        content.addClassName("stat-content");
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(FlexComponent.Alignment.CENTER);

        Div card = new Div(content);
        card.addClassName("stat-card");
        card.addClassName(className);

        return card;
    }

    private Component createFiltersSection() {
        searchField = new TextField();
        searchField.addClassName("search-field");
        searchField.setPlaceholder("Rechercher par code ou nom du client...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("350px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateGrid());

        statusFilter = new ComboBox<>("Filtrer par statut");
        statusFilter.addClassName("status-filter");
        statusFilter.setItems(StatutReservation.values());
        statusFilter.setItemLabelGenerator(StatutReservation::getLabel);
        statusFilter.setPlaceholder("Tous les statuts");
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("250px");
        statusFilter.addValueChangeListener(e -> updateGrid());

        Button resetButton = new Button("Réinitialiser", VaadinIcon.REFRESH.create());
        resetButton.addClassName("reset-button");
        resetButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        resetButton.addClickListener(e -> {
            searchField.clear();
            statusFilter.clear();
            updateGrid();
        });

        HorizontalLayout filters = new HorizontalLayout(searchField, statusFilter, resetButton);
        filters.addClassName("filters-section");
        filters.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        filters.setWidthFull();

        return filters;
    }

    private Component createGridSection() {
        grid = new Grid<>(Reservation.class, false);
        grid.addClassName("reservations-grid");
        grid.setHeight("600px");

        grid.addColumn(Reservation::getCodeReservation)
                .setHeader("Code Réservation")
                .setWidth("160px")
                .setFlexGrow(0);

        grid.addColumn(reservation -> reservation.getUtilisateur().getNomComplet())
                .setHeader("Client")
                .setFlexGrow(1);

        grid.addColumn(reservation -> reservation.getUtilisateur().getEmail())
                .setHeader("Email")
                .setFlexGrow(1);

        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places")
                .setWidth("100px")
                .setFlexGrow(0);

        grid.addColumn(reservation -> String.format("%.2f DH", reservation.getMontantTotal()))
                .setHeader("Montant")
                .setWidth("130px")
                .setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
                .setHeader("Statut")
                .setWidth("160px")
                .setFlexGrow(0);

        grid.addColumn(reservation -> reservation.getDateReservation().format(dateFormatter))
                .setHeader("Date de Réservation")
                .setWidth("180px")
                .setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(this::createActionsButtons))
                .setHeader("Actions")
                .setWidth("140px")
                .setFlexGrow(0);

        Div gridContainer = new Div(grid);
        gridContainer.addClassName("grid-container");

        return gridContainer;
    }

    private Component createStatusBadge(Reservation reservation) {
        Span badge = new Span(reservation.getStatut().getLabel());
        badge.addClassName("status-badge");

        // Ajouter une classe CSS basée sur le statut
        String statusClass = switch (reservation.getStatut()) {
            case CONFIRMEE -> "status-confirmed";
            case EN_ATTENTE -> "status-pending";
            case ANNULEE -> "status-cancelled";
        };
        badge.addClassName(statusClass);

        return badge;
    }

    private Component createActionsButtons(Reservation reservation) {
        Button confirmButton = new Button(VaadinIcon.CHECK.create());
        confirmButton.addClassName("action-button");
        confirmButton.addClassName("confirm-button");
        confirmButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        confirmButton.getElement().setProperty("title", "Confirmer la réservation");
        confirmButton.setEnabled(reservation.getStatut() == StatutReservation.EN_ATTENTE);
        confirmButton.addClickListener(e -> confirmReservation(reservation));

        Button viewButton = new Button(VaadinIcon.EYE.create());
        viewButton.addClassName("action-button");
        viewButton.addClassName("view-button");
        viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        viewButton.getElement().setProperty("title", "Voir les détails");
        viewButton.addClickListener(e -> viewDetails(reservation));

        HorizontalLayout actions = new HorizontalLayout(confirmButton, viewButton);
        actions.setSpacing(true);
        actions.setPadding(false);

        return actions;
    }

    private void confirmReservation(Reservation reservation) {
        try {
            reservationService.confirmerReservation(reservation.getId());

            Notification notification = Notification.show(
                    "✅ Réservation confirmée avec succès",
                    3000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            refreshData();

        } catch (Exception e) {
            Notification notification = Notification.show(
                    "❌ Erreur: " + e.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void viewDetails(Reservation reservation) {
        Notification.show(
                reservation.getRecapitulatif(),
                5000,
                Notification.Position.MIDDLE
        );
    }

    private void updateGrid() {
        String searchTerm = searchField.getValue().toLowerCase();
        StatutReservation selectedStatus = statusFilter.getValue();

        List<Reservation> filteredReservations = allReservations.stream()
                .filter(reservation -> {
                    boolean matchesSearch = searchTerm.isEmpty() ||
                            reservation.getCodeReservation().toLowerCase().contains(searchTerm) ||
                            reservation.getUtilisateur().getNomComplet().toLowerCase().contains(searchTerm);

                    boolean matchesStatus = selectedStatus == null ||
                            reservation.getStatut() == selectedStatus;

                    return matchesSearch && matchesStatus;
                })
                .toList();

        grid.setItems(filteredReservations);
    }

    private void refreshData() {
        allReservations = reservationService.getReservationsEvenement(event.getId());
        updateGrid();
    }
}