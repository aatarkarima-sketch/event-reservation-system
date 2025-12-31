package com.event.event_reservation_system.views;

import com.event.event_reservation_system.modele.User;
import com.event.event_reservation_system.security.SecurityUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

/**
 * Layout unifié qui s'adapte automatiquement selon le rôle de l'utilisateur connecté.
 * - Non connecté : Navigation publique simple
 * - Client : Navigation client avec dropdown
 * - Organisateur : Navigation organisateur avec dropdown
 * - Admin : Navigation admin avec dropdown
 */
@CssImport("./styles/unified-layout.css")
public class UnifiedLayout extends AppLayout {

    private final SecurityUtils securityUtils;
    private Div menuDropdown;
    private boolean isMenuOpen = false;

    public UnifiedLayout(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
        addClassName("unified-layout");
        createAdaptiveHeader();
    }

    private void createAdaptiveHeader() {
        securityUtils.getCurrentUser().ifPresentOrElse(
                this::createAuthenticatedHeader,
                this::createPublicHeader
        );
    }

    // ==================== HEADER PUBLIC (Non connecté) ====================
    private void createPublicHeader() {
        // Logo
        H2 logo = new H2("Evently");
        logo.addClassName("navbar-logo");
        logo.getStyle().set("cursor", "pointer");
        logo.addClickListener(e -> logo.getUI().ifPresent(ui -> ui.navigate("")));

        // Liens de navigation
        RouterLink homeLink = new RouterLink("Accueil", HomeView.class);
        homeLink.addClassName("nav-link");

        RouterLink eventsLink = new RouterLink("Événements", EventListView.class);
        eventsLink.addClassName("nav-link");

        Anchor contactLink = new Anchor("#contact", "Contact");
        contactLink.addClassName("nav-link");

        HorizontalLayout leftLinks = new HorizontalLayout(homeLink, eventsLink, contactLink);
        leftLinks.setSpacing(true);
        leftLinks.setAlignItems(FlexComponent.Alignment.CENTER);
        leftLinks.addClassName("nav-links");

        // Boutons connexion/inscription
        Button loginBtn = new Button("Connexion");
        loginBtn.addClassName("login-btn");
        loginBtn.addClickListener(e -> loginBtn.getUI().ifPresent(ui -> ui.navigate("login")));

        Button registerBtn = new Button("S'inscrire");
        registerBtn.addClassName("register-btn");
        registerBtn.addClickListener(e -> registerBtn.getUI().ifPresent(ui -> ui.navigate("register")));

        HorizontalLayout rightSection = new HorizontalLayout(loginBtn, registerBtn);
        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSection.setSpacing(true);
        rightSection.addClassName("auth-buttons");

        // Header complet
        HorizontalLayout header = new HorizontalLayout(logo, leftLinks, rightSection);
        header.addClassName("modern-navbar");
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        addToNavbar(header);
    }

    // ==================== HEADER AUTHENTIFIÉ (Connecté) ====================
    private void createAuthenticatedHeader(User user) {
        // Détermine le type de header selon le rôle
        String role = determineUserRole(user);

        // Bouton Menu
        Button menuButton = new Button(VaadinIcon.MENU.create());
        menuButton.addClassName("menu-button");
        menuButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        menuButton.addClickListener(e -> toggleMenu());

        // Logo
        H2 logo = new H2("Evently");
        logo.addClassName("navbar-logo");
        logo.getStyle().set("cursor", "pointer");
        logo.addClickListener(e -> logo.getUI().ifPresent(ui -> ui.navigate(getDefaultRoute(role))));

        HorizontalLayout logoSection = new HorizontalLayout(menuButton, logo);
        logoSection.setAlignItems(FlexComponent.Alignment.CENTER);
        logoSection.addClassName("logo-section");

        // Navigation centrale (seulement pour Client)
        HorizontalLayout navButtons = null;
        if ("CLIENT".equals(role)) {

        }

        // Section utilisateur
        Avatar avatar = new Avatar(user.getPrenom() + " " + user.getNom());
        avatar.setColorIndex(Math.abs(user.getEmail().hashCode()) % 8);
        avatar.addClassName("user-avatar");

        Span userName = new Span(user.getPrenom() + " " + user.getNom());
        userName.addClassName("user-name");

        Span userRole = new Span(getRoleDisplayName(role));
        userRole.addClassName("user-role");

        VerticalLayout userInfo = new VerticalLayout(userName, userRole);
        userInfo.addClassName("user-info");
        userInfo.setPadding(false);
        userInfo.setSpacing(false);

        HorizontalLayout userSection = new HorizontalLayout(avatar, userInfo);
        userSection.setAlignItems(FlexComponent.Alignment.CENTER);
        userSection.addClassName("user-section");

        // Assemblage du navbar
        HorizontalLayout navbar;
        if (navButtons != null) {
            navbar = new HorizontalLayout(logoSection, navButtons, userSection);
        } else {
            navbar = new HorizontalLayout(logoSection, userSection);
        }

        navbar.addClassName("modern-navbar"); // Même classe que HomeView
        navbar.setWidthFull();
        navbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);

        // Menu dropdown
        menuDropdown = createRoleBasedDropdown(user, role);

        // Container
        VerticalLayout navbarContainer = new VerticalLayout(navbar, menuDropdown);
        navbarContainer.addClassName("navbar-container");
        navbarContainer.setPadding(false);
        navbarContainer.setSpacing(false);

        addToNavbar(navbarContainer);
    }

    // ==================== NAVIGATION CLIENT ====================


    private Button createNavButton(VaadinIcon icon, String text, String route) {
        Button button = new Button(text, icon.create());
        button.addClassName("nav-button");
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.addClickListener(e -> {
            button.getUI().ifPresent(ui -> ui.navigate(route));
            closeMenu();
        });
        return button;
    }

    // ==================== MENU DROPDOWN ADAPTATIF ====================
    private Div createRoleBasedDropdown(User user, String role) {
        Div dropdown = new Div();
        dropdown.addClassName("menu-dropdown");
        dropdown.addClassName("hidden");

        // Info utilisateur (mobile)
        dropdown.add(createUserInfoMobile(user, role));

        // Sections selon le rôle
        switch (role) {
            case "CLIENT":
                dropdown.add(
                        createClientNavigationSection(),
                        createAccountSection(),
                        createLogoutSection(user)
                );
                break;

            case "ORGANIZER":
                dropdown.add(
                        createOrganizerNavigationSection(),
                        createAccountSection(),
                        createLogoutSection(user)
                );
                break;

            case "ADMIN":
                dropdown.add(
                        createAdminNavigationSection(),
                        createAccountSection(),
                        createLogoutSection(user)
                );
                break;
        }

        return dropdown;
    }

    // ==================== USER INFO MOBILE ====================
    private Div createUserInfoMobile(User user, String role) {
        Div userInfoMobile = new Div();
        userInfoMobile.addClassName("user-info-mobile");

        Avatar avatarMobile = new Avatar(user.getPrenom() + " " + user.getNom());
        avatarMobile.setColorIndex(Math.abs(user.getEmail().hashCode()) % 8);
        avatarMobile.getStyle().set("width", "64px").set("height", "64px");

        Span nameMobile = new Span(user.getPrenom() + " " + user.getNom());
        nameMobile.addClassName("user-name-mobile");

        Span emailMobile = new Span(user.getEmail());
        emailMobile.addClassName("user-email-mobile");

        Span roleMobile = new Span(getRoleDisplayName(role));
        roleMobile.addClassName("user-role-mobile");

        VerticalLayout userTextMobile = new VerticalLayout(nameMobile, emailMobile, roleMobile);
        userTextMobile.setPadding(false);
        userTextMobile.setSpacing(false);

        HorizontalLayout userCardMobile = new HorizontalLayout(avatarMobile, userTextMobile);
        userCardMobile.setAlignItems(FlexComponent.Alignment.CENTER);
        userCardMobile.addClassName("user-card-mobile");

        userInfoMobile.add(userCardMobile);
        return userInfoMobile;
    }

    // ==================== SECTIONS NAVIGATION CLIENT ====================
    private Div createClientNavigationSection() {
        Div section = new Div();
        section.addClassName("menu-section");

        Span title = new Span("NAVIGATION");
        title.addClassName("menu-section-title");

        section.add(
                title,
                createMenuItem(VaadinIcon.HOME, "Accueil", "Page d'accueil", () -> navigateTo("")),
                createMenuItem(VaadinIcon.CALENDAR, "Événements", "Découvrir les événements", () -> navigateTo("events")),
                createMenuItem(VaadinIcon.DASHBOARD, "Mon espace", "Tableau de bord", () -> navigateTo("client-dashboard")),
                createMenuItem(VaadinIcon.TICKET, "Mes réservations", "Gérer mes réservations", () ->
                        UI.getCurrent().navigate("my-reservations"))
        );

        return section;
    }

    // ==================== SECTIONS NAVIGATION ORGANISATEUR ====================
    private Div createOrganizerNavigationSection() {
        Div section = new Div();
        section.addClassName("menu-section");

        Span title = new Span("NAVIGATION");
        title.addClassName("menu-section-title");

        section.add(
                title,
                createMenuItem(VaadinIcon.DASHBOARD, "Dashboard", "Vue d'ensemble", () -> navigateTo("organizer/dashboard")),
                createMenuItem(VaadinIcon.CALENDAR, "Mes événements", "Gérer mes événements", () -> navigateTo("organizer/events")),
                createMenuItem(VaadinIcon.PLUS_CIRCLE, "Créer un événement", "Nouvel événement", () -> navigateTo("organizer/event/new"))
        );

        return section;
    }

    // ==================== SECTIONS NAVIGATION ADMIN ====================
    private Div createAdminNavigationSection() {
        Div section = new Div();
        section.addClassName("menu-section");

        Span title = new Span("NAVIGATION");
        title.addClassName("menu-section-title");

        section.add(
                title,
                createMenuItem(VaadinIcon.DASHBOARD, "Dashboard", "Vue d'ensemble", () -> navigateTo("admin/dashboard")),
                createMenuItem(VaadinIcon.USERS, "Utilisateurs", "Gérer les utilisateurs", () -> navigateTo("admin/users")),
                createMenuItem(VaadinIcon.CALENDAR, "Événements", "Gérer les événements", () -> navigateTo("admin/events")),
                createMenuItem(VaadinIcon.COG, "Réservations", "Gérer les réservations", () -> navigateTo("admin/reservations"))
        );

        return section;
    }

    // ==================== SECTION COMPTE ====================
    private Div createAccountSection() {
        Div section = new Div();
        section.addClassName("menu-section");

        Span title = new Span("COMPTE");
        title.addClassName("menu-section-title");

        section.add(
                title,
                createMenuItem(VaadinIcon.USER_CARD, "Mon profil", "Gérer mes informations", () -> navigateTo("profile"))
        );

        return section;
    }

    // ==================== SECTION DÉCONNEXION ====================
    private Div createLogoutSection(User user) {
        Div section = new Div();
        section.addClassName("menu-section");
        section.addClassName("logout-section");

        Button logoutBtn = new Button("Se déconnecter", VaadinIcon.SIGN_OUT.create());
        logoutBtn.addClassName("logout-button");
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        logoutBtn.addClickListener(e -> {
            securityUtils.logout();
            UI.getCurrent().navigate("");
        });

        section.add(logoutBtn);
        return section;
    }

    // ==================== CRÉATION ITEM MENU ====================
    private Button createMenuItem(VaadinIcon icon, String title, String description, Runnable action) {
        Icon itemIcon = icon.create();
        itemIcon.addClassName("menu-item-icon");

        Span itemTitle = new Span(title);
        itemTitle.addClassName("menu-item-title");

        Span itemDesc = new Span(description);
        itemDesc.addClassName("menu-item-description");

        VerticalLayout itemText = new VerticalLayout(itemTitle, itemDesc);
        itemText.addClassName("menu-item-text");
        itemText.setPadding(false);
        itemText.setSpacing(false);

        HorizontalLayout itemContent = new HorizontalLayout(itemIcon, itemText);
        itemContent.setAlignItems(FlexComponent.Alignment.CENTER);
        itemContent.setWidthFull();

        Button menuItem = new Button();
        menuItem.addClassName("menu-item");
        menuItem.getElement().appendChild(itemContent.getElement());
        menuItem.addClickListener(e -> {
            action.run();
            closeMenu();
        });

        return menuItem;
    }

    // ==================== UTILITAIRES ====================
    private void toggleMenu() {
        isMenuOpen = !isMenuOpen;

        if (isMenuOpen) {
            menuDropdown.removeClassName("hidden");
            menuDropdown.addClassName("visible");
        } else {
            menuDropdown.removeClassName("visible");
            menuDropdown.addClassName("hidden");
        }
    }

    private void closeMenu() {
        if (menuDropdown != null) {
            isMenuOpen = false;
            menuDropdown.removeClassName("visible");
            menuDropdown.addClassName("hidden");
        }
    }

    private void navigateTo(String route) {
        getUI().ifPresent(ui -> ui.navigate(route));
    }

    private String determineUserRole(User user) {
        if (user.getRole() == null) {
            return "CLIENT";
        }

        return switch (user.getRole()) {
            case ADMIN -> "ADMIN";
            case ORGANIZER -> "ORGANIZER";
            case CLIENT -> "CLIENT";
        };
    }


    private String getRoleDisplayName(String role) {
        switch (role) {
            case "ADMIN":
                return "Administrateur";
            case "ORGANIZER":
                return "Organisateur";
            case "CLIENT":
            default:
                return "Client";
        }
    }

    private String getDefaultRoute(String role) {
        switch (role) {
            case "ADMIN":
                return "admin/dashboard";
            case "ORGANIZER":
                return "organizer/dashboard";
            case "CLIENT":
                return "client-dashboard";
            default:
                return "";
        }
    }
}