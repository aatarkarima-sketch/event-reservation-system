package com.event.event_reservation_system.views;

import com.event.event_reservation_system.modele.User;
import com.event.event_reservation_system.security.SecurityUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
@CssImport("./styles/main-layout.css")
public class MainLayout extends AppLayout {

    private final SecurityUtils securityUtils;

    public MainLayout(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
        createHeader();
    }

    private void createHeader() {
        // === LOGO ===
        H2 logo = new H2("EventManager");
        logo.addClassName("navbar-logo-caveat");
        logo.getStyle().set("cursor", "pointer");
        logo.addClickListener(e -> logo.getUI().ifPresent(ui -> ui.navigate("")));

        // === LIENS GAUCHE ===
        RouterLink homeLink = new RouterLink("Accueil", HomeView.class);
        homeLink.addClassName("nav-simple-link");

        RouterLink eventsLink = new RouterLink("Événements", EventListView.class);
        eventsLink.addClassName("nav-simple-link");

        // Pour un lien vers une ancre/fragment dans la page, utilisez Anchor
        Anchor contactLink = new Anchor("#contact", "Contact");
        contactLink.addClassName("nav-simple-link");

        HorizontalLayout leftLinks = new HorizontalLayout(homeLink, eventsLink, contactLink);
        leftLinks.setSpacing(true);
        leftLinks.setAlignItems(FlexComponent.Alignment.CENTER);

        // === DROITE : Connecté ou pas ? ===
        HorizontalLayout rightSection = new HorizontalLayout();
        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSection.setSpacing(true);

        securityUtils.getCurrentUser().ifPresentOrElse(
                user -> {
                    // CONNECTÉ
                    Span welcome = new Span("Bonjour, " + user.getPrenom());
                    welcome.getStyle().set("color", "#26658C").set("font-weight", "600");

                    Button logout = new Button("Déconnexion", VaadinIcon.SIGN_OUT.create());
                    logout.addClassName("login-btn-minimal");
                    logout.addClickListener(e -> {
                        securityUtils.logout();
                        UI.getCurrent().navigate("login");
                    });

                    rightSection.add(welcome, logout);
                },
                () -> {
                    // NON CONNECTÉ
                    Button login = new Button("Connexion");
                    login.addClassName("login-btn-minimal");
                    login.addClickListener(e -> login.getUI().ifPresent(ui -> ui.navigate("login")));

                    Button register = new Button("S'inscrire");
                    register.addClassName("register-btn-navbar");
                    register.addClickListener(e -> register.getUI().ifPresent(ui -> ui.navigate("register")));

                    rightSection.add(login, register);
                }
        );

        // === HEADER COMPLET ===
        HorizontalLayout header = new HorizontalLayout(logo, leftLinks, rightSection);
        header.addClassName("navbar-content");
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(true);

        addToNavbar(header);
    }
}
