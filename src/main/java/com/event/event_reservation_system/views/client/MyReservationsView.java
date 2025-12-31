package com.event.event_reservation_system.views.client;

import com.event.event_reservation_system.modele.Reservation;
import com.event.event_reservation_system.modele.StatutReservation;
import com.event.event_reservation_system.modele.User;
import com.event.event_reservation_system.security.SecurityUtils;
import com.event.event_reservation_system.service.ReservationService;
import com.event.event_reservation_system.views.UnifiedLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.dependency.CssImport;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "my-reservations", layout = UnifiedLayout.class)
@PageTitle("Mes Réservations | Event Manager")
@CssImport("./styles/my-reservations-view.css")
@PermitAll
public class MyReservationsView extends VerticalLayout {
    private final ReservationService reservationService;
    private final SecurityUtils securityUtils;
    private final DateTimeFormatter fullDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private final DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy");

    private Grid<Reservation> grid;
    private TextField searchField;
    private Tabs statusTabs;
    private List<Reservation> allReservations;
    private StatutReservation currentFilter = null;

    private Tab tabTous;
    private Tab tabAttente;
    private Tab tabConfirmee;
    private Tab tabAnnulee;

    public MyReservationsView(ReservationService reservationService, SecurityUtils securityUtils) {
        this.reservationService = reservationService;
        this.securityUtils = securityUtils;

        addClassName("my-reservations-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);

        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Utilisateur non connecté"));

        allReservations = reservationService.getReservationsUtilisateur(currentUser.getId());

        add(
                createPageTitle(),
                createSearchSection(),
                createStatusTabs(),
                createReservationsGrid(),
                createPagination()
        );

        updateGrid();
    }

    private Component createPageTitle() {
        H1 title = new H1("Mes Réservations");
        title.addClassName("page-title");
        return title;
    }

    private Component createSearchSection() {
        Div wrapper = new Div();
        wrapper.addClassName("search-section");

        searchField = new TextField();
        searchField.setPlaceholder("Recherche par code de réservation");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addClassName("search-input");
        searchField.addValueChangeListener(e -> updateGrid());

        Button searchBtn = new Button("Rechercher");
        searchBtn.addClassName("search-button");

        wrapper.add(searchField, searchBtn);
        return wrapper;
    }

    private Component createStatusTabs() {
        tabTous = new Tab("Tous");
        tabAttente = new Tab("En attente");
        tabConfirmee = new Tab("Confirmée");
        tabAnnulee = new Tab("Annulée");

        statusTabs = new Tabs(tabTous, tabAttente, tabConfirmee, tabAnnulee);
        statusTabs.addClassName("status-tabs");
        statusTabs.setSelectedTab(tabTous);

        statusTabs.addSelectedChangeListener(event -> {
            Tab selected = statusTabs.getSelectedTab();
            if (selected == tabTous) currentFilter = null;
            else if (selected == tabAttente) currentFilter = StatutReservation.EN_ATTENTE;
            else if (selected == tabConfirmee) currentFilter = StatutReservation.CONFIRMEE;
            else if (selected == tabAnnulee) currentFilter = StatutReservation.ANNULEE;
            updateGrid();
        });

        updateTabCounts();
        return statusTabs;
    }

    private void updateTabCounts() {
        long attente = allReservations.stream().filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE).count();
        long confirmee = allReservations.stream().filter(r -> r.getStatut() == StatutReservation.CONFIRMEE).count();
        long annulee = allReservations.stream().filter(r -> r.getStatut() == StatutReservation.ANNULEE).count();
        long total = allReservations.size();

        tabTous.setLabel("Tous (" + total + ")");
        tabAttente.setLabel("En attente (" + attente + ")");
        tabConfirmee.setLabel("Confirmée (" + confirmee + ")");
        tabAnnulee.setLabel("Annulée (" + annulee + ")");
    }

    private Component createReservationsGrid() {
        grid = new Grid<>(Reservation.class, false);
        grid.addClassName("reservations-grid");

        // SOLUTION: Définir une hauteur minimale pour afficher plusieurs lignes
        grid.setHeight("600px"); // Hauteur fixe pour afficher environ 5-6 événements

        // OU ALTERNATIVE: Utiliser setPageSize au lieu de setAllRowsVisible
        // grid.setPageSize(10); // Affiche 10 lignes par page

        // Colonne Événement (image + titre + date)
        grid.addColumn(new ComponentRenderer<>(this::createEventColumn))
                .setHeader("Événement")
                .setFlexGrow(3)
                .setAutoWidth(true);

        // Date de l'événement
        grid.addColumn(res -> res.getEvenement().getDateDebut().format(shortDateFormatter))
                .setHeader("Date")
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setAutoWidth(true);

        // Places
        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places")
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setAutoWidth(true);

        // Montant
        grid.addColumn(res -> String.format("%.0f€", res.getMontantTotal()))
                .setHeader("Montant")
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setAutoWidth(true);

        // Statut
        grid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
                .setHeader("Statut")
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setAutoWidth(true);

        // Action Annuler
        grid.addColumn(new ComponentRenderer<>(this::createCancelButton))
                .setHeader("Actions")
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setAutoWidth(true);

        return grid;
    }

    private Component createEventColumn(Reservation reservation) {
        Div container = new Div();
        container.addClassName("event-column");

        Image img = new Image(
                reservation.getEvenement().getImageUrl() != null
                        ? reservation.getEvenement().getImageUrl()
                        : "https://via.placeholder.com/80x80?text=Event",
                reservation.getEvenement().getTitre()
        );
        img.addClassName("event-image-small");

        Div text = new Div();
        text.addClassName("event-text");

        Span title = new Span(reservation.getEvenement().getTitre());
        title.addClassName("event-title");

        Span date = new Span(reservation.getEvenement().getDateDebut().format(fullDateFormatter));
        date.addClassName("event-date");

        text.add(title, date);
        container.add(img, text);

        return container;
    }

    private Component createStatusBadge(Reservation reservation) {
        Span badge = new Span(reservation.getStatut().getLabel());
        badge.addClassName("status-badge");
        badge.addClassName("status-" + reservation.getStatut().name().toLowerCase());
        return badge;
    }

    private Component createCancelButton(Reservation reservation) {
        Button cancelBtn = new Button("Annuler");
        cancelBtn.addClassName("cancel-button");

        if (reservation.getStatut() == StatutReservation.ANNULEE || !reservation.peutEtreAnnulee()) {
            cancelBtn.setEnabled(false);
            cancelBtn.addClassName("disabled");
        }

        cancelBtn.addClickListener(e -> confirmCancellation(reservation));
        return cancelBtn;
    }

    private void confirmCancellation(Reservation reservation) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Annuler la réservation ?");
        dialog.setText("Êtes-vous sûr de vouloir annuler la réservation pour l'événement \""
                + reservation.getEvenement().getTitre() + "\" ?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Oui, annuler");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> performCancellation(reservation));
        dialog.open();
    }

    private void performCancellation(Reservation reservation) {
        try {
            User currentUser = securityUtils.getCurrentUser().orElseThrow();
            reservationService.annulerReservation(reservation.getId(), currentUser.getId());

            Notification.show("Réservation annulée avec succès", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            allReservations = reservationService.getReservationsUtilisateur(currentUser.getId());
            updateGrid();
        } catch (Exception ex) {
            Notification.show("Erreur : " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateGrid() {
        String searchTerm = searchField.getValue() == null ? "" : searchField.getValue().toLowerCase().trim();

        List<Reservation> filtered = allReservations.stream()
                .filter(res -> currentFilter == null || res.getStatut() == currentFilter)
                .filter(res -> searchTerm.isEmpty() ||
                        res.getCodeReservation().toLowerCase().contains(searchTerm))
                .toList();

        grid.setItems(filtered);
        updateTabCounts();
    }

    private Component createPagination() {
        Div pagination = new Div();
        pagination.addClassName("pagination-container");

        Span pageInfo = new Span("Page 1 sur 1");

        Button prevBtn = new Button(new Icon(VaadinIcon.ARROW_LEFT));
        prevBtn.setEnabled(false);

        Span pages = new Span("1 2 3 ... 10");

        Button nextBtn = new Button("Suivant");
        nextBtn.setIcon(VaadinIcon.ARROW_RIGHT.create());
        nextBtn.setIconAfterText(true);

        pagination.add(prevBtn, pageInfo, pages, nextBtn);

        return pagination;
    }
}