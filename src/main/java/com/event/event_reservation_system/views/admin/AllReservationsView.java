package com.event.event_reservation_system.views.admin;

import com.event.event_reservation_system.modele.*;
import com.event.event_reservation_system.service.ReservationService;
import com.event.event_reservation_system.service.UserService;
import com.event.event_reservation_system.views.UnifiedLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "admin/reservations", layout = UnifiedLayout.class)
@PageTitle("Gestion des Réservations | Event Manager")
@CssImport("./styles/admin-reservations-management.css")
@RolesAllowed("ADMIN")
public class AllReservationsView extends VerticalLayout {

    private final ReservationService reservationService;
    private final UserService userService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private Grid<Reservation> grid;
    private List<Reservation> allReservations;
    private List<Reservation> filteredReservations;

    // Filtres
    private ComboBox<StatutReservation> statusFilter;
    private TextField codeFilter;
    private TextField userFilter;
    private TextField eventFilter;
    private DatePicker dateFrom;
    private DatePicker dateTo;

    public AllReservationsView(ReservationService reservationService, UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;

        // Classe CSS principale + taille pleine page
        addClassName("admin-reservations-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        allReservations = reservationService.listerToutesAvecUtilisateurEtEvenement();
        filteredReservations = allReservations;

        // === Conteneur principal qui prend tout l'espace ===
        VerticalLayout mainCard = new VerticalLayout();
        mainCard.addClassName("main-card-full");
        mainCard.setWidthFull();
        mainCard.setFlexGrow(1);        // Important : grandit dans la vue
        mainCard.setPadding(false);
        mainCard.setSpacing(false);

        // 1. Filtres (hauteur fixe)
        Component filters = createFiltersSection();
        mainCard.add(filters);

        // 2. Tableau (prend tout l'espace restant)
        Component gridComponent = createGridSection();
        mainCard.add(gridComponent);
        mainCard.setFlexGrow(1, gridComponent);  // ← Le grid grandit !

        // 3. Footer avec stats, export et pagination
        VerticalLayout footer = new VerticalLayout(
                createStatsSection(),
                createExportSection(),
                createPaginationSection()
        );
        footer.setWidthFull();
        footer.setSpacing(false);
        footer.setPadding(false);
        footer.addClassName("dashboard-footer");

        mainCard.add(footer);
        // Le footer ne grandit pas → flexGrow 0 par défaut

        // Ajout du conteneur principal à la vue
        add(mainCard);
        setFlexGrow(1, mainCard);  // La carte prend tout l'espace

        updateGrid();
    }

    private Component createFiltersSection() {
        // === Champs de filtre ===
        statusFilter = new ComboBox<>("Filtrer par statut:");
        statusFilter.setItems(StatutReservation.values());
        statusFilter.setItemLabelGenerator(StatutReservation::getLabel);
        statusFilter.setPlaceholder("Tous les statuts");
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidthFull();
        statusFilter.addValueChangeListener(e -> applyFilters());

        codeFilter = new TextField("Rechercher par code:");
        codeFilter.setPlaceholder("Ex: RSV123456");
        codeFilter.setClearButtonVisible(true);
        codeFilter.setValueChangeMode(ValueChangeMode.LAZY);
        codeFilter.setWidthFull();
        codeFilter.addValueChangeListener(e -> applyFilters());

        userFilter = new TextField("Rechercher par utilisateur:");
        userFilter.setPlaceholder("Nom ou email");
        userFilter.setClearButtonVisible(true);
        userFilter.setValueChangeMode(ValueChangeMode.LAZY);
        userFilter.setWidthFull();
        userFilter.addValueChangeListener(e -> applyFilters());

        eventFilter = new TextField("Rechercher par événement:");
        eventFilter.setPlaceholder("Titre de l'événement");
        eventFilter.setClearButtonVisible(true);
        eventFilter.setValueChangeMode(ValueChangeMode.LAZY);
        eventFilter.setWidthFull();
        eventFilter.addValueChangeListener(e -> applyFilters());

        dateFrom = new DatePicker("Du:");
        dateFrom.setClearButtonVisible(true);
        dateFrom.setWidthFull();
        dateFrom.addValueChangeListener(e -> applyFilters());

        dateTo = new DatePicker("Au:");
        dateTo.setClearButtonVisible(true);
        dateTo.setWidthFull();
        dateTo.addValueChangeListener(e -> applyFilters());

        // === Boutons ===
        Button filterBtn = new Button("Filtrer", VaadinIcon.FILTER.create());
        filterBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        filterBtn.addClassName("filter-button"); // Pour personnaliser la couleur
        filterBtn.addClickListener(e -> applyFilters());

        Button resetBtn = new Button("Réinitialiser", VaadinIcon.REFRESH.create());
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetBtn.addClickListener(e -> resetFilters());

        HorizontalLayout buttonsLayout = new HorizontalLayout(filterBtn, resetBtn);
        buttonsLayout.setWidthFull();
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonsLayout.setAlignItems(FlexComponent.Alignment.END);

        // === Disposition des filtres en 2 lignes ===
        HorizontalLayout row1 = new HorizontalLayout(statusFilter, codeFilter, userFilter);
        row1.setWidthFull();
        row1.setFlexGrow(1, statusFilter, codeFilter, userFilter);

        HorizontalLayout row2 = new HorizontalLayout(eventFilter, dateFrom, dateTo, buttonsLayout);
        row2.setWidthFull();
        row2.setFlexGrow(1, eventFilter, dateFrom, dateTo);
        row2.setAlignItems(FlexComponent.Alignment.END); // Aligne les boutons en bas

        VerticalLayout filtersForm = new VerticalLayout(row1, row2);
        filtersForm.setSpacing(true);
        filtersForm.setPadding(false);

        // === Titre centré ===
        H3 title = new H3("Gestion des Réservations");
        title.addClassName("filters-title");

        // === Conteneur principal ===
        VerticalLayout container = new VerticalLayout(title, filtersForm);
        container.addClassName("filters-container");
        container.setPadding(true);
        container.setSpacing(false);
        container.setAlignItems(FlexComponent.Alignment.STRETCH);

        return container;
    }

    private Component createGridSection() {
        grid = new Grid<>(Reservation.class, false);
        grid.addClassName("reservations-grid-premium"); // Nouvelle classe CSS
        grid.setWidthFull();

        // 1. Code + Client (avec style premium)
        grid.addComponentColumn(res -> {
                    HorizontalLayout cell = new HorizontalLayout();
                    cell.setAlignItems(FlexComponent.Alignment.CENTER);
                    cell.setSpacing(true);

                    // Avatar moderne (optionnel mais classe)
                    Avatar avatar = new Avatar(res.getUtilisateur().getNomComplet());
                    avatar.addClassName("premium-avatar");
                    avatar.setColorIndex(Math.abs(res.getUtilisateur().getEmail().hashCode() % 12));

                    VerticalLayout info = new VerticalLayout();
                    info.setSpacing(false);
                    info.setPadding(false);

                    Span code = new Span(res.getCodeReservation());
                    code.addClassName("premium-code");

                    Span client = new Span(res.getUtilisateur().getNomComplet());
                    client.addClassName("premium-client");

                    info.add(code, client);
                    cell.add(avatar, info);

                    return cell;
                }).setHeader("Client")
                .setFlexGrow(2);

        // 2. Événement
        grid.addColumn(res -> res.getEvenement().getTitre())
                .setHeader("Événement")
                .setFlexGrow(3);

        // 3. Date
        grid.addColumn(res -> res.getEvenement().getDateDebut().format(dateFormatter))
                .setHeader("Date")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.CENTER);

        // 4. Places
        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.CENTER);

        // 5. Montant
        grid.addColumn(res -> String.format("%.0f €", res.getMontantTotal()))
                .setHeader("Montant")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END);

        // 6. Statut – badge ultra-moderne
        grid.addComponentColumn(res -> {
                    Span badge = new Span(res.getStatut().getLabel());
                    badge.addClassName("premium-status");
                    badge.addClassName("status-" + res.getStatut().name().toLowerCase());
                    return badge;
                }).setHeader("Statut")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.CENTER);

        // 7. Action – icône œil élégante
        grid.addComponentColumn(res -> {
                    Button eye = new Button(new Icon(VaadinIcon.EYE));
                    eye.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
                    eye.addClassName("premium-action-btn");

                    String recap = """
            <div style="font-family: system-ui, sans-serif; line-height: 1.6;">
                <strong>Réservation :</strong> %s<br>
                <strong>Client :</strong> %s (%s)<br>
                <strong>Événement :</strong> %s<br>
                <strong>Date :</strong> %s<br>
                <strong>Places :</strong> %d<br>
                <strong>Montant :</strong> %.0f €<br>
                <strong>Statut :</strong> %s
            </div>
            """.formatted(
                            res.getCodeReservation(),
                            res.getUtilisateur().getNomComplet(),
                            res.getUtilisateur().getEmail(),
                            res.getEvenement().getTitre(),
                            res.getEvenement().getDateDebut().format(dateFormatter),
                            res.getNombrePlaces(),
                            res.getMontantTotal(),
                            res.getStatut().getLabel()
                    );

                    eye.addClickListener(e ->
                            Notification.show(recap, 12000, Notification.Position.MIDDLE)
                                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY)
                    );

                    return eye;
                }).setHeader("Actions")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.setItems(filteredReservations);
        return grid;
    }

    private Component createStatsSection() {
        long total = filteredReservations.size();
        long confirmed = filteredReservations.stream().filter(r -> r.getStatut() == StatutReservation.CONFIRMEE).count();
        long canceled = filteredReservations.stream().filter(r -> r.getStatut() == StatutReservation.ANNULEE).count();
        double revenue = filteredReservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                .mapToDouble(Reservation::getMontantTotal)
                .sum();

        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        stats.setPadding(true);
        stats.addClassName("stats-container");

        stats.add(
                createStatCard("1231", "Réservations à venir", "#6366f1"),
                createStatCard("3044", "Réservations confirmées", "#10b981"),
                createStatCard("418", "Réservations annulées", "#ef4444", true),
                createStatCard(String.format("%.0f €", revenue), "Revenus Totaux", "#f59e0b")
        );

        return stats;
    }

    private Component createStatCard(String value, String label, String color, boolean isRed) {
        Div card = new Div();
        card.addClassName("stat-card");

        Span valSpan = new Span(value);
        valSpan.addClassName("value");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("label");

        card.add(valSpan, labelSpan);

        if (isRed) {
            card.getStyle().set("background", "#fee2e2");
            valSpan.getStyle().set("color", "#ef4444");
        } else {
            card.getStyle().set("background", "#f0fdf4");
            valSpan.getStyle().set("color", color);
        }

        return card;
    }

    private Component createStatCard(String value, String label, String color) {
        return createStatCard(value, label, color, false);
    }

    private Component createExportSection() {
        Button exportBtn = new Button("Exporter les données", VaadinIcon.DOWNLOAD.create());
        exportBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout container = new HorizontalLayout(exportBtn);
        container.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        container.setWidthFull();
        container.setPadding(true);
        container.addClassName("export-container");

        return container;
    }

    private Component createPaginationSection() {
        HorizontalLayout pagination = new HorizontalLayout();
        pagination.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        pagination.setWidthFull();
        pagination.addClassName("pagination");

        Button prev = new Button("← Page 1 sur 45");
        prev.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button next = new Button("Suivant →");
        next.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        pagination.add(prev, next);
        return pagination;
    }

    private void applyFilters() {
        filteredReservations = allReservations.stream()
                .filter(res -> statusFilter.getValue() == null || res.getStatut() == statusFilter.getValue())
                .filter(res -> codeFilter.isEmpty() || res.getCodeReservation().toLowerCase().contains(codeFilter.getValue().toLowerCase()))
                .filter(res -> userFilter.isEmpty() || res.getUtilisateur().getNomComplet().toLowerCase().contains(userFilter.getValue().toLowerCase())
                        || res.getUtilisateur().getEmail().toLowerCase().contains(userFilter.getValue().toLowerCase()))
                .filter(res -> eventFilter.isEmpty() || res.getEvenement().getTitre().toLowerCase().contains(eventFilter.getValue().toLowerCase()))
                .filter(res -> dateFrom.isEmpty() || !res.getDateReservation().toLocalDate().isBefore(dateFrom.getValue()))
                .filter(res -> dateTo.isEmpty() || !res.getDateReservation().toLocalDate().isAfter(dateTo.getValue()))
                .toList();

        updateGrid();
    }

    private void resetFilters() {
        statusFilter.clear();
        codeFilter.clear();
        userFilter.clear();
        eventFilter.clear();
        dateFrom.clear();
        dateTo.clear();
        filteredReservations = allReservations;
        updateGrid();
    }

    private void updateGrid() {
        grid.setItems(filteredReservations);
    }
}