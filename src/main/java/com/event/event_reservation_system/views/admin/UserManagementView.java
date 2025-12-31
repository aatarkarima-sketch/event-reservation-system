package com.event.event_reservation_system.views.admin;

import com.event.event_reservation_system.modele.Role;
import com.event.event_reservation_system.modele.User;
import com.event.event_reservation_system.service.UserService;
import com.event.event_reservation_system.views.MainLayout;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "admin/users", layout = UnifiedLayout.class)
@PageTitle("Gestion des Utilisateurs | Evently")
@CssImport("./styles/user-management-view.css")
@RolesAllowed("ADMIN")
public class UserManagementView extends VerticalLayout {

    private final UserService userService;
    private Grid<User> grid;
    private TextField searchField;
    private ComboBox<Role> roleFilter;
    private ComboBox<Boolean> statusFilter;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public UserManagementView(UserService userService) {
        this.userService = userService;

        addClassName("user-management-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        add(createHeader(), createFiltersBar(), createUserGrid());

        updateGrid();
    }

    private Component createHeader() {
        H2 title = new H2("Gestion des Utilisateurs");
        title.addClassName("page-header");
        return title;
    }

    private Component createFiltersBar() {
        searchField = new TextField();
        searchField.setPlaceholder("Recherche par nom ou email");
        searchField.setWidth("320px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateGrid());

        roleFilter = new ComboBox<>("Filtrer par rôle");
        roleFilter.setItems(Role.values());
        roleFilter.setItemLabelGenerator(Role::getLabel);
        roleFilter.setPlaceholder("Tous les rôles");
        roleFilter.setClearButtonVisible(true);
        roleFilter.addValueChangeListener(e -> updateGrid());

        statusFilter = new ComboBox<>("Tous les statuts");
        statusFilter.setItems(true, false);
        statusFilter.setItemLabelGenerator(b -> b ? "Actif" : "Inactif");
        statusFilter.setPlaceholder("Tous les statuts");
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> updateGrid());

        Button searchButton = new Button("Rechercher", VaadinIcon.SEARCH.create());
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClassName("search-button");
        searchButton.addClickListener(e -> updateGrid());

        HorizontalLayout left = new HorizontalLayout(roleFilter, statusFilter);
        left.setSpacing(true);
        left.setAlignItems(Alignment.END);

        HorizontalLayout filters = new HorizontalLayout(searchField, left, searchButton);
        filters.addClassName("filters-bar");
        filters.setWidthFull();
        filters.setJustifyContentMode(JustifyContentMode.BETWEEN);
        filters.setAlignItems(Alignment.END);

        return filters;
    }

    private Component createUserGrid() {
        grid = new Grid<>(User.class, false);
        grid.addClassName("user-grid");
        grid.setPageSize(10);
        grid.setHeight("680px");
        // Colonne Nom + Avatar
        grid.addComponentColumn(user -> {
            Div avatar = new Div(new Span(user.getPrenom().charAt(0) + "" + user.getNom().charAt(0)));
            avatar.addClassName("user-avatar");

            Span name = new Span(user.getNomComplet());
            name.getStyle().set("font-weight", "600");

            Div cell = new Div(avatar, name);
            cell.addClassName("name-cell");
            return cell;
        }).setHeader("Nom").setFlexGrow(1);

        // Email
        grid.addColumn(User::getEmail).setHeader("Email").setFlexGrow(1);

        // Rôle (badge)
        grid.addComponentColumn(user -> {
            Span badge = new Span(user.getRole().getLabel());
            badge.addClassName("role-badge");
            badge.getStyle().set("background-color", getRoleColor(user.getRole()));
            return badge;
        }).setHeader("Rôle");

        // Date inscription
        grid.addColumn(user -> user.getDateInscription().format(dateFormatter))
                .setHeader("Date Inscription");

        // Statut (badge)
        // Statut (badge)
        grid.addComponentColumn(user -> {
            Span badge = new Span(user.getActif() ? "Actif" : "Inactif");
            badge.addClassNames("status-badge", user.getActif() ? "status-active" : "status-inactive");
            return badge;
        }).setHeader("Statut");

        // Actions
        grid.addComponentColumn(this::createActions).setHeader("Actions");

        return grid;
    }

    private String getRoleColor(Role role) {
        return switch (role) {
            case ADMIN -> "#ef4444";
            case ORGANIZER -> "#3b82f6";
            case CLIENT -> "#10b981";
            default -> "#64748b";
        };
    }

    private Component createActions(User user) {
        Button detailsBtn = new Button("Voir détails", VaadinIcon.EYE.create());
        detailsBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        detailsBtn.addClassName("action-btn");
        detailsBtn.addClickListener(e -> showUserDetails(user));

        Button toggleBtn = new Button(user.getActif() ? "Désactiver" : "Activer");
        toggleBtn.addThemeVariants(user.getActif() ? ButtonVariant.LUMO_ERROR : ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        toggleBtn.addClassName("action-btn");
        toggleBtn.addClickListener(e -> toggleUserStatus(user));

        ComboBox<Role> roleCombo = new ComboBox<>();
        roleCombo.setItems(Role.values());
        roleCombo.setItemLabelGenerator(Role::getLabel);
        roleCombo.setValue(user.getRole());
        roleCombo.setWidth("140px");
        roleCombo.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() != user.getRole()) {
                changeUserRole(user, e.getValue());
            }
        });

        HorizontalLayout actions = new HorizontalLayout(detailsBtn, roleCombo, toggleBtn);
        actions.addClassName("actions-cell");
        actions.setSpacing(true);
        return actions;
    }

    private void showUserDetails(User user) {
        // Garde ta logique existante pour les détails
        Notification.show("Détails de " + user.getNomComplet() + " (à implémenter)", 3000, Notification.Position.MIDDLE);
    }

    private void changeUserRole(User user, Role newRole) {
        try {
            userService.changerRole(user.getId(), newRole);
            Notification.show("Rôle modifié avec succès", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Erreur lors du changement de rôle", 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void toggleUserStatus(User user) {
        try {
            if (user.getActif()) {
                userService.desactiverCompte(user.getId());
            } else {
                userService.activerCompte(user.getId());
            }
            Notification.show(user.getActif() ? "Utilisateur désactivé" : "Utilisateur activé", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Erreur lors du changement de statut", 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateGrid() {
        List<User> users = userService.listerTous();

        String search = searchField.getValue() != null ? searchField.getValue().toLowerCase() : "";
        Role role = roleFilter.getValue();
        Boolean actif = statusFilter.getValue();

        List<User> filtered = users.stream()
                .filter(u -> search.isEmpty() ||
                        u.getNomComplet().toLowerCase().contains(search) ||
                        u.getEmail().toLowerCase().contains(search))
                .filter(u -> role == null || u.getRole() == role)
                .filter(u -> actif == null || u.getActif() == actif)
                .toList();

        grid.setItems(filtered);
    }

}