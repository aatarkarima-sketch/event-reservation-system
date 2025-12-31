package com.event.event_reservation_system.views.admin;

import com.event.event_reservation_system.modele.*;
import com.event.event_reservation_system.service.EventService;
import com.event.event_reservation_system.service.UserService;
import com.event.event_reservation_system.views.UnifiedLayout;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "admin/events", layout = UnifiedLayout.class)
@PageTitle("Gestion des Événements | Event Manager")
@CssImport("./styles/admin-events-management.css")
@RolesAllowed("ADMIN")
public class AllEventsManagementView extends VerticalLayout {

    private final EventService eventService;
    private final UserService userService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private Grid<Event> grid;
    private static final int PAGE_SIZE = 5;
    private int currentPage = 0;
    private int totalPages = 1;
    // Filtres
    private ComboBox<User> organisateurFilter;
    private ComboBox<Categorie> categoryFilter;
    private DatePicker dateFrom;
    private DatePicker dateTo;
    private TextField villeField;
    private IntegerField priceMaxField;

    private List<Event> allEvents;

    public AllEventsManagementView(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;

        addClassName("admin-events-management-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        allEvents = eventService.listerTousAvecOrganisateur();

        add(
                createHeader(),
                createFiltersSection(),
                createGrid()
        );

        updateGrid();
    }

    private Component createHeader() {
        H1 title = new H1("Gestion des Événements");
        title.addClassName("page-title");
        return title;
    }

    private Component createFiltersSection() {



        organisateurFilter = new ComboBox<>("Organisateur");
        organisateurFilter.setItems(userService.listerTous().stream()
                .filter(u -> u.getRole() == Role.ORGANIZER || u.getRole() == Role.ADMIN)
                .toList());
        organisateurFilter.setItemLabelGenerator(User::getNomComplet);
        organisateurFilter.setClearButtonVisible(true);
        organisateurFilter.addValueChangeListener(e -> updateGrid());

        categoryFilter = new ComboBox<>("Catégorie");
        categoryFilter.setItems(Categorie.values());
        categoryFilter.setItemLabelGenerator(Categorie::getLabel);
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> updateGrid());

        dateFrom = new DatePicker("Date de début");
        dateFrom.setClearButtonVisible(true);
        dateFrom.addValueChangeListener(e -> updateGrid());

        dateTo = new DatePicker("Date de fin");
        dateTo.setClearButtonVisible(true);
        dateTo.addValueChangeListener(e -> updateGrid());

        villeField = new TextField("Ville");
        villeField.setClearButtonVisible(true);
        villeField.setValueChangeMode(ValueChangeMode.LAZY);
        villeField.addValueChangeListener(e -> updateGrid());

        priceMaxField = new IntegerField("Prix maximum");
        priceMaxField.setMin(0);
        priceMaxField.setMax(5000);
        priceMaxField.setValue(500);
        priceMaxField.addValueChangeListener(e -> updateGrid());

        Button filterBtn = new Button("Appliquer", VaadinIcon.FILTER.create());
        filterBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        filterBtn.addClickListener(e -> updateGrid());

        Button resetBtn = new Button("Réinitialiser", VaadinIcon.REFRESH.create());
        resetBtn.addClickListener(e -> resetFilters());

        HorizontalLayout buttons = new HorizontalLayout(filterBtn, resetBtn);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("1000px", 4)
        );

        form.add(
                organisateurFilter,
                categoryFilter,
                dateFrom,
                dateTo,
                villeField,
                priceMaxField,
                buttons
        );

        form.setColspan(organisateurFilter, 2);
        form.setColspan(categoryFilter, 2);
        form.setColspan(buttons, 4);

        VerticalLayout container = new VerticalLayout(form, buttons);
        container.addClassName("filters-section");
        container.setPadding(true);
        container.setSpacing(true);

        return container;
    }


    private Component createGrid() {
        grid = new Grid<>(Event.class, false);
        grid.addClassName("events-grid-simple");

        // Colonne Événement (image + titre + organisateur en dessous)
        grid.addComponentColumn(event -> {
            Div container = new Div();
            container.addClassName("event-cell");

            Image img = new Image();
            img.addClassName("event-thumbnail");

            String url = event.getImageUrl();
            if (url != null && !url.isBlank()) {
                img.setSrc(url);
            } else {
                // Placeholder propre si jamais null (mais normalement plus besoin)
                img.setSrc("https://via.placeholder.com/100x60/c9d1d9/6c757d?text=No+Image");
            }

            img.setAlt("Image de " + event.getTitre());

            Div textContent = new Div();
            textContent.addClassName("event-text");

            Span title = new Span(event.getTitre());
            title.addClassName("event-title");

            Span organizer = new Span(event.getOrganisateur().getNomComplet());
            organizer.addClassName("event-organizer");

            textContent.add(title, organizer);
            container.add(img, textContent);

            return container;
        }).setHeader("Événement").setFlexGrow(3);

        // Date
        grid.addColumn(event -> event.getDateDebut().format(dateFormatter))
                .setHeader("Date")
                .setAutoWidth(true);

        // Ville
        grid.addColumn(Event::getVille)
                .setHeader("Ville")
                .setAutoWidth(true);

        // Prix
        grid.addColumn(event -> event.getPrixUnitaire() + "€")
                .setHeader("Prix")
                .setAutoWidth(true);

        // Statut
        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Statut")
                .setAutoWidth(true);

        // Actions (menu déroulant simple)
        grid.addComponentColumn(this::createActions)
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Afficher tous les événements (pas de pagination)
        grid.setItems(allEvents);

        return grid;
    }

    private Component createStatusBadge(Event event) {
        Span badge = new Span(event.getStatut().getLabel());
        badge.addClassNames("status-badge","status-" + event.getStatut().name().toLowerCase());
        return badge;
    }

    private Component createActions(Event event) {
        Button viewBtn = new Button("Voir", VaadinIcon.EYE.create());
        viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        viewBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("event/" + event.getId())));

        Button menuBtn = new Button(VaadinIcon.ELLIPSIS_DOTS_V.create());
        menuBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

        VerticalLayout dropdown = new VerticalLayout();
        dropdown.addClassName("action-dropdown");
        dropdown.setVisible(false);

        dropdown.add(
                createMenuItem("Modifier", () -> getUI().ifPresent(ui -> ui.navigate("organizer/event/" + event.getId() + "/edit"))),
                createMenuItem("Publier", () -> publishEvent(event), event.getStatut() == Statut.BROUILLON),
                createMenuItem("Annuler", () -> confirmCancelEvent(event), event.getStatut() == Statut.PUBLIE),
                createMenuItem("Changer rôle", () -> {}),
                createMenuItem("Utiliser Organisateur", () -> {}),
                createMenuItem("Administrateur", () -> {}),
                createMenuItem("Désactiver", () -> {}),
                createMenuItem("Supprimer", () -> confirmDeleteEvent(event), VaadinIcon.TRASH, ButtonVariant.LUMO_ERROR)
        );

        menuBtn.addClickListener(e -> dropdown.setVisible(!dropdown.isVisible()));

        HorizontalLayout actions = new HorizontalLayout(viewBtn, menuBtn, dropdown);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        actions.setSpacing(true);

        return actions;
    }

    // === MÉTHODES createMenuItem - ORDRE CRITIQUE ===
    private Button createMenuItem(String text, Runnable action) {
        return createMenuItem(text, action, true, null, (ButtonVariant[]) null);
    }

    private Button createMenuItem(String text, Runnable action, boolean enabled) {
        return createMenuItem(text, action, enabled, null, (ButtonVariant[]) null);
    }

    private Button createMenuItem(String text, Runnable action, VaadinIcon icon, ButtonVariant... variants) {
        return createMenuItem(text, action, true, icon, variants);
    }

    private Button createMenuItem(String text, Runnable action, boolean enabled, VaadinIcon icon, ButtonVariant... variants) {
        Button btn = new Button(text, icon != null ? icon.create() : null);
        btn.addClickListener(e -> action.run());
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        if (variants != null && variants.length > 0) {
            btn.addThemeVariants(variants);
        }
        btn.setEnabled(enabled);
        return btn;
    }

    private void publishEvent(Event event) {
        Notification.show("Événement publié", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        refreshData();
    }

    private Component createPagination() {
        HorizontalLayout pagination = new HorizontalLayout();
        pagination.addClassName("pagination-modern");

        Button prevBtn = new Button("Précédent", VaadinIcon.ARROW_LEFT.create());
        prevBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        prevBtn.setEnabled(currentPage > 0);
        prevBtn.addClickListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateGrid();
            }
        });

        Span pageInfo = new Span(String.format("Page %d sur %d", currentPage + 1, totalPages));
        pageInfo.addClassName("page-info");

        Button nextBtn = new Button("Suivant", VaadinIcon.ARROW_RIGHT.create());
        nextBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        nextBtn.setEnabled(currentPage < totalPages - 1);
        nextBtn.addClickListener(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                updateGrid();
            }
        });

        // Ajoute les numéros de page (max 5 visibles pour simplicité)
        HorizontalLayout pageNumbers = new HorizontalLayout();
        pageNumbers.setSpacing(true);
        int startPage = Math.max(0, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + 5);

        for (int i = startPage; i < endPage; i++) {
            int pageIndex = i;
            Button pageBtn = new Button(String.valueOf(i + 1));
            pageBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            if (i == currentPage) {
                pageBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }
            pageBtn.addClickListener(e -> {
                currentPage = pageIndex;
                updateGrid();
            });
            pageNumbers.add(pageBtn);
        }

        pagination.add(prevBtn, pageInfo, pageNumbers, nextBtn);
        pagination.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        pagination.setAlignItems(FlexComponent.Alignment.CENTER);
        pagination.setWidthFull();
        pagination.setPadding(true);

        return pagination;
    }

    private void updateGrid() {
        calculatePages(); // Au cas où les filtres ont changé la taille de la liste

        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allEvents.size());

        List<Event> pageEvents = allEvents.subList(start, end);
        grid.setItems(pageEvents);

        // Met à jour la pagination (on le fera dans createPagination si besoin)
    }

    private void resetFilters() {
        organisateurFilter.clear();
        categoryFilter.clear();
        dateFrom.clear();
        dateTo.clear();
        villeField.clear();
        priceMaxField.setValue(500);
        currentPage = 0; // Retour à la première page
        updateGrid();
    }

    private void confirmCancelEvent(Event event) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Annuler l'événement");
        dialog.setText("Êtes-vous sûr de vouloir annuler cet événement ?");
        dialog.setConfirmText("Oui, annuler");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            eventService.annulerEvenement(event.getId(), null);
            Notification.show("Événement annulé", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            refreshData();
        });
        dialog.open();
    }

    private void confirmDeleteEvent(Event event) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Supprimer l'événement");
        dialog.setText("Supprimer définitivement ?");
        dialog.setConfirmText("Oui, supprimer");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            eventService.supprimerEvenement(event.getId(), null);
            Notification.show("Événement supprimé", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            refreshData();
        });
        dialog.open();
    }

    private void refreshData() {
        allEvents = eventService.listerTousAvecOrganisateur();
        calculatePages();
        updateGrid();
    }
    private void calculatePages() {
        if (allEvents.isEmpty()) {
            totalPages = 1;
        } else {
            totalPages = (int) Math.ceil((double) allEvents.size() / PAGE_SIZE);
        }
        // Réinitialise à la première page si la page actuelle est hors limites
        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }
    }
}