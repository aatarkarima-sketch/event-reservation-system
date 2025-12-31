package com.event.event_reservation_system.views.organizer;

import com.event.event_reservation_system.modele.Event;
import com.event.event_reservation_system.modele.Statut;
import com.event.event_reservation_system.modele.User;
import com.event.event_reservation_system.security.SecurityUtils;
import com.event.event_reservation_system.service.EventService;
import com.event.event_reservation_system.service.ReservationService;
import com.event.event_reservation_system.views.UnifiedLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "organizer/events", layout = UnifiedLayout.class)
@PageTitle("Mes Événements | Event Manager")
@RolesAllowed({"ORGANIZER", "ADMIN"})
@CssImport("./styles/my-events-view.css")
public class MyEventsView extends VerticalLayout {

    private final EventService eventService;
    private final ReservationService reservationService;
    private final SecurityUtils securityUtils;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy à HH:mm");

    private Grid<Event> grid;
    private ComboBox<Statut> statusFilter;
    private List<Event> allEvents;

    // Pagination
    private int currentPage = 0;
    private static final int PAGE_SIZE = 10;
    private int totalPages = 1;

    private Button previousPageBtn;
    private Button nextPageBtn;
    private Span pageInfo;

    public MyEventsView(EventService eventService, ReservationService reservationService, SecurityUtils securityUtils) {
        this.eventService = eventService;
        this.reservationService = reservationService;
        this.securityUtils = securityUtils;

        addClassName("my-events-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Utilisateur non connecté"));

        allEvents = eventService.trouverParOrganisateur(currentUser);

        // Conteneur principal centré
        Div mainContainer = new Div();
        mainContainer.addClassName("main-container");
        mainContainer.setWidthFull();

        mainContainer.add(
                createHeader(),
                createFilters(),
                createGridSection(),
                createPagination()
        );

        add(mainContainer);
        setFlexGrow(1, mainContainer);

        updateGrid();
    }

    private Component createHeader() {
        H2 title = new H2("Mes Événements");
        title.addClassName("page-title");

        Button createButton = new Button("Créer un événement", VaadinIcon.PLUS.create());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClassName("create-event-btn");
        createButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("organizer/event/new")));

        HorizontalLayout header = new HorizontalLayout(title, createButton);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.addClassName("page-header");

        return header;
    }

    private Component createFilters() {
        statusFilter = new ComboBox<>("Filtrer par statut");
        statusFilter.setItems(Statut.values());
        statusFilter.setItemLabelGenerator(Statut::getLabel);
        statusFilter.setPlaceholder("Tous les statuts");
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> {
            currentPage = 0;
            updateGrid();
        });

        Button resetButton = new Button("Réinitialiser", VaadinIcon.REFRESH.create());
        resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetButton.addClickListener(e -> {
            statusFilter.clear();
            currentPage = 0;
            updateGrid();
        });

        HorizontalLayout filters = new HorizontalLayout(statusFilter, resetButton);
        filters.setAlignItems(FlexComponent.Alignment.END);
        filters.addClassName("filters-bar");

        return filters;
    }

    private Component createGridSection() {
        grid = new Grid<>(Event.class, false);
        grid.addClassName("events-grid-modern");

        // Titre + catégorie
        grid.addColumn(new ComponentRenderer<>(event -> {
            VerticalLayout cell = new VerticalLayout();
            cell.setSpacing(false);
            cell.setPadding(false);

            Span title = new Span(event.getTitre());
            title.addClassName("event-title-grid");

            Span category = new Span(event.getCategorie().getLabel());
            category.addClassName("event-category-grid");

            cell.add(title, category);
            return cell;
        })).setHeader("Événement").setFlexGrow(2);

        // Date
        grid.addColumn(event -> event.getDateDebut().format(dateFormatter))
                .setHeader("Date & Heure")
                .setAutoWidth(true)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER);

        // Statut
        grid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
                .setHeader("Statut")
                .setAutoWidth(true)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER);

        // Capacité
        grid.addColumn(new ComponentRenderer<>(this::createCapacityProgress))
                .setHeader("Places")
                .setWidth("220px");

        // Actions
        grid.addColumn(new ComponentRenderer<>(this::createActionsButtons))
                .setHeader("Actions")
                .setAutoWidth(true)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER);

        Div gridWrapper = new Div(grid);
        gridWrapper.addClassName("grid-wrapper");

        return gridWrapper;
    }

    private Component createStatusBadge(Event event) {
        Span badge = new Span(event.getStatut().getLabel());
        badge.addClassName("status-badge");
        badge.addClassName("status-" + event.getStatut().name().toLowerCase());
        return badge;
    }

    private Component createCapacityProgress(Event event) {
        int reserved = event.getPlacesReservees();
        int total = event.getCapaciteMax();
        double percentage = total > 0 ? (double) reserved / total : 0;

        ProgressBar bar = new ProgressBar();
        bar.setValue(percentage);
        bar.addClassName("capacity-progress");

        Span text = new Span(reserved + " / " + total);
        text.addClassName("capacity-text");

        VerticalLayout layout = new VerticalLayout(bar, text);
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        return layout;
    }

    private Component createActionsButtons(Event event) {
        Button view = new Button(VaadinIcon.EYE.create());
        view.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY); // Changé en TERTIARY
        view.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("event/detail/" + event.getId())));

        Button edit = new Button(VaadinIcon.EDIT.create());
        edit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        edit.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("organizer/event/edit/" + event.getId())));

        Button reservations = new Button(VaadinIcon.TICKET.create());
        reservations.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        reservations.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("organizer/event/" + event.getId() + "/reservations")));

        HorizontalLayout actions = new HorizontalLayout(view, edit, reservations);
        actions.setSpacing(true);
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        return actions;
    }

    private Component createPagination() {
        previousPageBtn = new Button(VaadinIcon.ANGLE_LEFT.create());
        previousPageBtn.addThemeVariants(ButtonVariant.LUMO_LARGE);
        previousPageBtn.addClickListener(e -> {
            if (currentPage > 0) currentPage--;
            updateGrid();
        });

        nextPageBtn = new Button(VaadinIcon.ANGLE_RIGHT.create());
        nextPageBtn.addThemeVariants(ButtonVariant.LUMO_LARGE);
        nextPageBtn.addClickListener(e -> {
            if (currentPage < totalPages - 1) currentPage++;
            updateGrid();
        });

        pageInfo = new Span();
        pageInfo.addClassName("page-info");

        HorizontalLayout pagination = new HorizontalLayout(previousPageBtn, pageInfo, nextPageBtn);
        pagination.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        pagination.setWidthFull();
        pagination.addClassName("pagination-bar");

        return pagination;
    }

    private void updateGrid() {
        Statut selected = statusFilter.getValue();
        List<Event> filtered = allEvents.stream()
                .filter(e -> selected == null || e.getStatut() == selected)
                .toList();

        totalPages = (int) Math.ceil((double) filtered.size() / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;
        currentPage = Math.min(currentPage, totalPages - 1);

        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filtered.size());

        grid.setItems(filtered.subList(start, end));

        pageInfo.setText("Page " + (currentPage + 1) + " sur " + totalPages);
        previousPageBtn.setEnabled(currentPage > 0);
        nextPageBtn.setEnabled(currentPage < totalPages - 1);
    }
}