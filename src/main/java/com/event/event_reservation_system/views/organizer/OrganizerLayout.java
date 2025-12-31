package com.event.event_reservation_system.views.organizer;

import com.event.event_reservation_system.modele.User;
import com.event.event_reservation_system.security.SecurityUtils;
import com.event.event_reservation_system.views.HomeView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

@CssImport("./styles/organizer-layout.css")
public class OrganizerLayout extends AppLayout {

    private final SecurityUtils securityUtils;
    private Div menuDropdown;
    private boolean isMenuOpen = false;

    public OrganizerLayout(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
        addClassName("organizer-layout");
        createHeader();
    }

    private void createHeader() {
        // === LOGO SECTION ===


        H2 logo = new H2("EventManager");
        logo.addClassName("navbar-logo");
        logo.getStyle().set("cursor", "pointer");
        logo.addClickListener(e -> logo.getUI().ifPresent(ui -> ui.navigate(HomeView.class)));



        // === USER SECTION (À DROITE) ===
        User user = securityUtils.getCurrentUser().orElseThrow();

        // Avatar avec gradient personnalisé
        Avatar avatar = new Avatar(user.getPrenom() + " " + user.getNom());
        avatar.setColorIndex(Math.abs(user.getEmail().hashCode()) % 8);
        avatar.addClassName("user-avatar");

        // Nom de l'utilisateur (visible sur desktop uniquement)
        Span userName = new Span(user.getPrenom() + " " + user.getNom());
        userName.addClassName("user-name");

        Span userRole = new Span("Organisateur");
        userRole.addClassName("user-role");

        VerticalLayout userInfo = new VerticalLayout(userName, userRole);
        userInfo.addClassName("user-info");
        userInfo.setPadding(false);
        userInfo.setSpacing(false);

        // Bouton Menu avec icône
        Button menuButton = new Button(VaadinIcon.MENU.create());
        menuButton.addClassName("menu-button");
        menuButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        menuButton.addClickListener(e -> toggleMenu());

        // Modifier logoSection pour inclure le bouton menu
        HorizontalLayout logoSection = new HorizontalLayout(menuButton, logo);
        logoSection.setAlignItems(FlexComponent.Alignment.CENTER);
        logoSection.setSpacing(true);
        logoSection.addClassName("logo-section");

        // Modifier userSection pour retirer le menuButton
        HorizontalLayout userSection = new HorizontalLayout(avatar, userInfo);
        userSection.setAlignItems(FlexComponent.Alignment.CENTER);
        userSection.setSpacing(true);
        userSection.addClassName("user-section");

        // === NAVBAR PRINCIPALE ===
        HorizontalLayout navbar = new HorizontalLayout(logoSection, userSection);
        navbar.addClassName("organizer-navbar");
        navbar.setWidthFull();
        navbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);

        // === MENU DROPDOWN (initialement caché) ===
        menuDropdown = createMenuDropdown(user);

        // Container pour navbar + dropdown
        VerticalLayout navbarContainer = new VerticalLayout(navbar, menuDropdown);
        navbarContainer.addClassName("navbar-container");
        navbarContainer.setPadding(false);
        navbarContainer.setSpacing(false);

        addToNavbar(navbarContainer);
    }

    private Div createMenuDropdown(User user) {
        Div dropdown = new Div();
        dropdown.addClassName("menu-dropdown");
        dropdown.addClassName("hidden");

        // === SECTION NAVIGATION ===
        Div navSection = new Div();
        navSection.addClassName("menu-section");

        Span navTitle = new Span("NAVIGATION");
        navTitle.addClassName("menu-section-title");

        Button dashboardBtn = createMenuItem(
                VaadinIcon.DASHBOARD,
                "Dashboard",
                "Vue d'ensemble de vos événements",
                () -> navigateTo("organizer/dashboard")
        );

        Button myEventsBtn = createMenuItem(
                VaadinIcon.CALENDAR,
                "Mes événements",
                "Gérer tous vos événements",
                () -> navigateTo("organizer/events")
        );

        Button createEventBtn = createMenuItem(
                VaadinIcon.PLUS_CIRCLE,
                "Créer un événement",
                "Ajouter un nouvel événement",
                () -> navigateTo("organizer/event/new")
        );

        navSection.add(navTitle, dashboardBtn, myEventsBtn, createEventBtn);

        // === SECTION COMPTE ===
        Div accountSection = new Div();
        accountSection.addClassName("menu-section");

        Span accountTitle = new Span("COMPTE");
        accountTitle.addClassName("menu-section-title");

        Button profileBtn = createMenuItem(
                VaadinIcon.USER_CARD,
                "Mon profil",
                "Gérer vos informations",
                () -> navigateTo("profile")
        );

        Button settingsBtn = createMenuItem(
                VaadinIcon.COG,
                "Paramètres",
                "Configuration du compte",
                () -> {
                    // TODO: Implémenter la page paramètres
                    closeMenu();
                }
        );

        accountSection.add(accountTitle, profileBtn, settingsBtn);

        // === BOUTON DÉCONNEXION ===
        Div logoutSection = new Div();
        logoutSection.addClassName("menu-section");
        logoutSection.addClassName("logout-section");

        Button logoutBtn = new Button("Se déconnecter", VaadinIcon.SIGN_OUT.create());
        logoutBtn.addClassName("logout-button");
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        logoutBtn.addClickListener(e -> {
            securityUtils.logout();
            UI.getCurrent().navigate("");
        });

        logoutSection.add(logoutBtn);

        // === USER INFO DANS LE MENU (visible sur mobile) ===
        Div userInfoMobile = new Div();
        userInfoMobile.addClassName("user-info-mobile");

        Avatar avatarMobile = new Avatar(user.getPrenom() + " " + user.getNom());
        avatarMobile.setColorIndex(Math.abs(user.getEmail().hashCode()) % 8);
        avatarMobile.getStyle().set("width", "48px").set("height", "48px");

        Span nameMobile = new Span(user.getPrenom() + " " + user.getNom());
        nameMobile.addClassName("user-name-mobile");

        Span emailMobile = new Span(user.getEmail());
        emailMobile.addClassName("user-email-mobile");

        VerticalLayout userTextMobile = new VerticalLayout(nameMobile, emailMobile);
        userTextMobile.setPadding(false);
        userTextMobile.setSpacing(false);

        HorizontalLayout userCardMobile = new HorizontalLayout(avatarMobile, userTextMobile);
        userCardMobile.setAlignItems(FlexComponent.Alignment.CENTER);
        userCardMobile.addClassName("user-card-mobile");

        userInfoMobile.add(userCardMobile);

        // === ASSEMBLAGE DU DROPDOWN ===
        dropdown.add(userInfoMobile, navSection, accountSection, logoutSection);

        return dropdown;
    }

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
        isMenuOpen = false;
        menuDropdown.removeClassName("visible");
        menuDropdown.addClassName("hidden");
    }

    private void navigateTo(String route) {
        getUI().ifPresent(ui -> ui.navigate(route));
    }
}