package com.event.event_reservation_system.views;

import com.event.event_reservation_system.dto.EventDTO;
import com.event.event_reservation_system.modele.Categorie;
import com.event.event_reservation_system.service.EventService;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.component.html.Anchor;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route("")
@PageTitle("Découvrez des Événements Inoubliables | EventManager")
@AnonymousAllowed
@CssImport("./styles/home-view.css")
public class HomeView extends VerticalLayout {

    private final EventService eventService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final Div eventsContainer = new Div();
    private String selectedCategory = "Tous";

    public HomeView(EventService eventService) {
        this.eventService = eventService;

        setPadding(false);
        setMargin(false);
        setSpacing(false);
        setSizeFull();

        addClassName("modern-home-view");

        add(
                createNavbar(),
                createHeroSection(),
                createCategoriesSection(),
                createEventsSection(),
                createTestimonialsSection(),
                createFooter()
        );

        // === CLÉ : Le footer est poussé en bas si le contenu est court ===
        setFlexGrow(1, this); // Toute la vue grandit
        getElement().getStyle()
                .set("min-height", "100vh")
                .set("display", "flex")
                .set("flex-direction", "column");

        // Animation subtile pour le bouton (effet de brillance)
        getElement().executeJs("""
    const ctaBtn = document.querySelector('.hero-cta-minimal');
    if (ctaBtn) {
        ctaBtn.addEventListener('mouseenter', function(e) {
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;
            
            ripple.style.cssText = `
                position: absolute;
                width: ${size}px;
                height: ${size}px;
                left: ${x}px;
                top: ${y}px;
                background: radial-gradient(circle, rgba(255,255,255,0.6) 0%, transparent 70%);
                border-radius: 50%;
                transform: scale(0);
                animation: ripple 0.6s ease-out;
                pointer-events: none;
            `;
            
            this.appendChild(ripple);
            setTimeout(() => ripple.remove(), 600);
        });
    }
    
    // Ajout du style pour l'effet ripple
    const style = document.createElement('style');
    style.textContent = `
        @keyframes ripple {
            to { transform: scale(2); opacity: 0; }
        }
    `;
    document.head.appendChild(style);
""");

        loadPopularEvents();
    }

    private Component createNavbar() {
        Div navbar = new Div();
        navbar.addClassName("modern-navbar");

        // === LOGO ===
        H2 logo = new H2("Evently");
        logo.addClassName("navbar-logo");
        logo.getStyle().set("cursor", "pointer");
        logo.addClickListener(e -> logo.getUI().ifPresent(ui -> ui.navigate("")));

        // === LIENS GAUCHE ===
        Anchor eventsLink = new Anchor("events", "Événements");
        eventsLink.addClassName("nav-simple-link");

        Anchor contactLink = new Anchor("#contact", "Contact");
        contactLink.addClassName("nav-simple-link");

        // === BOUTONS DROITE : Connexion + S'inscrire ===
        Button loginBtn = new Button("Connexion");
        loginBtn.addClassName("login-btn-minimal");
        loginBtn.addClickListener(e -> loginBtn.getUI().ifPresent(ui -> ui.navigate("login")));

        Button registerBtn = new Button("S'inscrire");
        registerBtn.addClassName("register-btn-navbar");
        registerBtn.addClickListener(e -> registerBtn.getUI().ifPresent(ui -> ui.navigate("register")));

        HorizontalLayout authButtons = new HorizontalLayout(loginBtn, registerBtn);
        authButtons.setSpacing(true);
        authButtons.setAlignItems(FlexComponent.Alignment.CENTER);

        // === LAYOUT FINAL ===
        HorizontalLayout left = new HorizontalLayout(eventsLink, contactLink);
        left.setSpacing(true);
        left.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout navContent = new HorizontalLayout(logo, left, authButtons);
        navContent.setWidthFull();
        navContent.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        navContent.setAlignItems(FlexComponent.Alignment.CENTER);
        navContent.addClassName("navbar-content");

        navbar.add(navContent);
        return navbar;
    }

    private Component createHeroSection() {
        Div hero = new Div();
        hero.addClassName("hero-fullwidth");

        // Contenu centré
        Div content = new Div();
        content.addClassName("hero-content");

        H1 title = new H1("Evently");
        title.addClassName("hero-title");

        Paragraph subtitle = new Paragraph("Découvrez des expériences uniques qui créent des souvenirs inoubliables");
        subtitle.addClassName("hero-subtitle");

        Button getStarted = new Button("Get Started");
        getStarted.addClassName("hero-cta");
        getStarted.addClickListener(e -> getStarted.getUI().ifPresent(ui -> ui.navigate("events")));

        content.add(title, subtitle, getStarted);
        hero.add(content);

        return hero;
    }

    // ==================== CATEGORIES ====================
    private Component createCategoriesSection() {
        Div section = new Div();
        section.addClassName("categories-fullwidth");

        H2 title = new H2("Parcourir par catégorie");
        title.addClassName("section-title");

        // Grille des cartes
        Div grid = new Div();
        grid.addClassName("categories-grid-full");

        // Liste des catégories + images Unsplash (qualité pro)
        var categories = List.of(
                new Category("Concert",      "/images/fistivale.jpg"),
                new Category("Théâtre", "/images/theatre.jpg"),
                new Category("Conférence", "/images/Corporate Event Creativity - conferences.jpg"),
                new Category("Sport", "/images/Sport.jpg")
        );

        for (Category cat : categories) {
            Div card = createCategoryImageCard(cat.name, cat.imageUrl);
            grid.add(card);
        }

        section.add(title, grid);
        return section;
    }

    // Classe helper simple
    private record Category(String name, String imageUrl) {}

    // Création d'une carte avec image de fond
    private Div createCategoryImageCard(String name, String imageUrl) {
        Div card = new Div();
        card.addClassName("category-card-full");

        // Image de fond
        card.getStyle()
                .set("background-image", "url('" + imageUrl + "')")
                .set("background-size", "cover")
                .set("background-position", "center");

        // Overlay sombre
        Div overlay = new Div();
        overlay.addClassName("category-overlay-full");

        // Nom de la catégorie
        Span label = new Span(name);
        label.addClassName("category-label-full");

        overlay.add(label);
        card.add(overlay);

        // Clic → navigation avec filtre catégorie
        card.addClickListener(e -> {
            card.getUI().ifPresent(ui -> {
                try {
                    Categorie cat = Categorie.fromString(name);
                    ui.navigate("events?category=" + cat.name());
                } catch (Exception ex) {
                    ui.navigate("events?category=" + name.toLowerCase());
                }
            });
        });

        return card;
    }

    private Component createEventsSection() {
        // Conteneur principal pleine largeur
        Div section = new Div();
        section.addClassName("events-fullwidth");

        // === En-tête centré ===
        VerticalLayout header = new VerticalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);  // Centre horizontalement
        header.setSpacing(false);
        header.setPadding(false);

        H2 title = new H2("Événements populaires");
        title.addClassName("events-title");

        Paragraph subtitle = new Paragraph("Les événements les plus attendus du moment");
        subtitle.addClassName("events-subtitle");

        header.add(title, subtitle);

        // === Grid des événements (3 en preview) ===
        Div grid = new Div();
        grid.addClassName("events-grid-full");
        loadOnlyThreeEvents(grid);  // Ta méthode qui charge les 3 événements

        // === Bouton "Voir tous les événements" centré ===
        Button seeMore = new Button("Voir tous les événements", VaadinIcon.ARROW_RIGHT.create());
        seeMore.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        seeMore.addClassName("see-more-btn");
        seeMore.addClickListener(e ->
                seeMore.getUI().ifPresent(ui -> ui.navigate("events"))
        );

        Div buttonWrapper = new Div(seeMore);
        buttonWrapper.addClassName("see-more-wrapper");
        buttonWrapper.setWidthFull();

        // Assemblage final
        section.add(header, grid, buttonWrapper);

        return section;
    }

    private void loadOnlyThreeEvents(Div grid) {
        grid.removeAll();

        // Squelettes pendant le chargement
        for (int i = 0; i < 3; i++) {
            grid.add(createSkeletonCard());
        }

        UI.getCurrent().access(() -> {
            List<EventDTO> events = eventService.getEvenementsPopulairesDTO(3); // ← seulement 3

            grid.removeAll();

            if (events != null && !events.isEmpty()) {
                events.forEach(event -> grid.add(createEventCard(event)));
            } else {
                Paragraph noEvents = new Paragraph("Aucun événement pour le moment");
                noEvents.addClassName("no-events");
                grid.add(noEvents);
            }
        });
    }

    private void loadPopularEvents() {
        eventsContainer.removeAll();
        for (int i = 0; i < 6; i++) {
            eventsContainer.add(createSkeletonCard());
        }

        UI.getCurrent().access(() -> {
            List<EventDTO> events = eventService.getEvenementsPopulairesDTO(6);
            eventsContainer.removeAll();
            if (events != null && !events.isEmpty()) {
                events.forEach(event -> eventsContainer.add(createEventCard(event)));
            } else {
                Paragraph noEvents = new Paragraph("Aucun événement disponible pour le moment");
                noEvents.addClassName("no-events");
                eventsContainer.add(noEvents);
            }
        });
    }

    private void filterEventsByCategory(String category) {
        eventsContainer.removeAll();
        for (int i = 0; i < 6; i++) {
            eventsContainer.add(createSkeletonCard());
        }

        UI.getCurrent().access(() -> {
            List<EventDTO> events = "Tous".equals(category)
                    ? eventService.getEvenementsPopulairesDTO(6)
                    : eventService.getEvenementsByCategorie(category, 6);

            eventsContainer.removeAll();
            if (events != null && !events.isEmpty()) {
                events.forEach(event -> eventsContainer.add(createEventCard(event)));
            } else {
                Paragraph noEvents = new Paragraph("Aucun événement dans cette catégorie");
                noEvents.addClassName("no-events");
                eventsContainer.add(noEvents);
            }
        });
    }

    private Component createEventCard(EventDTO event) {
        Anchor link = new Anchor("details/" + event.getId(), "");
        link.setClassName("event-link");

        Div card = new Div();
        card.addClassName("event-card-modern");

        // === IMAGE + DATE BADGE RONDE ===
        Div imageWrapper = new Div();
        imageWrapper.addClassName("event-image-wrapper-modern");

        String imgUrl = (event.getImageUrl() != null && !event.getImageUrl().isBlank())
                ? event.getImageUrl()
                : getCategoryImage(event.getCategorie().getLabel());

        imageWrapper.getStyle()
                .set("background-image", "url('" + imgUrl + "')")
                .set("background-size", "cover")
                .set("background-position", "center");

        // Badge date ronde et élégante
        Div dateBadge = new Div();
        dateBadge.addClassName("event-date-badge-modern");
        Span day = new Span(String.valueOf(event.getDateDebut().getDayOfMonth()));
        day.addClassName("date-day-modern");
        Span month = new Span(event.getDateDebut().format(DateTimeFormatter.ofPattern("MMM")).toUpperCase());
        month.addClassName("date-month-modern");
        dateBadge.add(day, month);
        imageWrapper.add(dateBadge);

        // === CONTENU ===
        Div content = new Div();
        content.addClassName("event-content-modern");

        // Catégorie
        Span category = new Span(event.getCategorie().getLabel());
        category.addClassName("event-category-modern");

        // Titre
        H3 title = new H3(event.getTitre());
        title.addClassName("event-title-modern");

        // Ville + heure
        Div meta = new Div();
        meta.addClassName("event-meta-modern");
        meta.add(
                new Span(VaadinIcon.MAP_MARKER.create(), new Span(" " + event.getVille())),
                new Span(VaadinIcon.CLOCK.create(), new Span(" " + event.getDateDebut().format(DateTimeFormatter.ofPattern("HH:mm"))))
        );

        // Prix + places
        Div footer = new Div();
        footer.addClassName("event-footer-modern");

        Span price = new Span(String.format("%.0f DH", event.getPrixUnitaire()));
        price.addClassName("event-price-modern");

        Span places = new Span(event.getPlacesDisponibles() + " places");
        places.addClassName("event-places-modern");

        footer.add(price, places);

        content.add(category, title, meta, footer);
        card.add(imageWrapper, content);
        link.add(card);

        return link;
    }


    // ==================== TESTIMONIALS ====================
    private Component createTestimonialsSection() {
        Div section = new Div();
        section.addClassName("testimonials-section-new");

        H2 title = new H2("Ce que disent nos utilisateurs");
        title.addClassName("section-title-new");

        Div grid = new Div();
        grid.addClassName("testimonials-grid-new");

        grid.add(
                createTestimonialCard("Sarah M.", "Casablanca", "Une expérience incroyable ! J'ai trouvé les meilleurs concerts grâce à cette plateforme.", 5),
                createTestimonialCard("Karim B.", "Rabat", "Réservation super simple et événements de qualité. Je recommande vivement !", 5),
                createTestimonialCard("Amina L.", "Marrakech", "Interface moderne et intuitive. J'ai réservé pour 5 événements ce mois-ci !", 5)
        );

        section.add(title, grid);
        return section;
    }

    private Component createTestimonialCard(String name, String city, String text, int stars) {
        Div card = new Div();
        card.addClassName("testimonial-card-new");

        Div starsDiv = new Div();
        starsDiv.addClassName("stars");
        for (int i = 0; i < 5; i++) {
            Span star = new Span(i < stars ? "★" : "☆");
            star.addClassName("star");
            if (i < stars) star.addClassName("filled");
            starsDiv.add(star);
        }

        Paragraph quote = new Paragraph(text);
        quote.addClassName("quote-text");

        Div author = new Div();
        author.addClassName("author-info");
        Span avatar = new Span(name.substring(0, 1).toUpperCase());
        avatar.addClassName("avatar");
        Div info = new Div();
        Span authorName = new Span(name);
        authorName.addClassName("author-name");

        Span authorCity = new Span(city);
        authorCity.addClassName("author-city");

        info.add(authorName, authorCity);        author.add(avatar, info);

        card.add(starsDiv, quote, author);
        return card;
    }

    // ==================== STATS SECTION ====================
    // ==================== STATS SECTION ====================


    private Component createStat(String value, String label, VaadinIcon icon) {
        Div card = new Div();
        card.addClassName("stat-card-compact");

        Icon i = icon.create();
        i.addClassName("stat-icon-compact");

        Span val = new Span(value);
        val.addClassName("stat-value-compact");

        Span lbl = new Span(label);
        lbl.addClassName("stat-label-compact");

        card.add(i, val, lbl);
        return card;
    }

    // ==================== FOOTER - CORRIGÉ ====================
    // ==================== FOOTER - VERSION FINALE & CORRIGÉE ====================
    private Component createFooter() {
        Footer footer = new Footer();
        footer.addClassName("modern-footer-new");

        Div content = new Div();
        content.addClassName("footer-content-new");

        // ── Colonne 1 ──
        Div col1 = new Div();
        col1.addClassName("footer-col");
        H3 logo = new H3("EventManager");
        logo.addClassName("footer-logo");

        Paragraph desc = new Paragraph(
                "La plateforme leader pour découvrir et réserver les meilleurs événements au Maroc."
        );
        desc.addClassName("footer-desc");

        col1.add(logo, desc);

        // ── Colonne 2 ──
        Div col2 = new Div();
        col2.addClassName("footer-col");
        H4 linksTitle = new H4("Liens rapides");
        linksTitle.addClassName("footer-subtitle-new");
        Div links = new Div(
                createFooterLink("Événements", "events"),
                createFooterLink("À propos", "#"),
                createFooterLink("Contact", "#contact"),
                createFooterLink("CGU", "#")
        );
        links.addClassName("footer-links-new");
        col2.add(linksTitle, links);

        // ── Colonne 3 ──
        Div col3 = new Div();
        col3.addClassName("footer-col");
        H4 contactTitle = new H4("Contact");
        contactTitle.addClassName("footer-subtitle-new");
        Div contactInfo = new Div(
                new Paragraph("contact@eventmanager.ma"),
                new Paragraph("+212 5XX-XXXXXX"),
                new Paragraph("Casablanca, Maroc")
        );
        contactInfo.addClassName("footer-contact-new");
        col3.add(contactTitle, contactInfo);

        // ── Colonne 4 ── (icônes réseaux sociaux corrigées)
        Div col4 = new Div();
        col4.addClassName("footer-col");
        H4 socialTitle = new H4("Suivez-nous");
        socialTitle.addClassName("footer-subtitle-new");

        HorizontalLayout social = new HorizontalLayout(
                createSocialBtn(VaadinIcon.FACEBOOK),
                createSocialBtn(VaadinIcon.CAMERA),
                createSocialBtn(VaadinIcon.TWITTER),
                createSocialBtn(VaadinIcon.LINK)   // LINKEDIN existe bien dans Vaadin 24+
        );
        social.addClassName("social-buttons");
        col4.add(socialTitle, social);

        // Ajout des colonnes
        content.add(col1, col2, col3, col4);

        // Copyright
        Div copyright = new Div();
        copyright.addClassName("footer-copyright-new");
        copyright.add("© 2025 EventManager • Tous droits réservés");

        footer.add(content, copyright);
        footer.getElement().setAttribute("id", "contact");
        return footer;
    }

    // Méthode utilitaire (inchangée)
    private Anchor createFooterLink(String text, String route) {
        Anchor a = new Anchor(route, text);
        a.addClassName("footer-link-new");
        return a;
    }

    // Méthode utilitaire corrigée
    private Button createSocialBtn(VaadinIcon icon) {
        Button btn = new Button(icon.create());
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn.addClassName("social-btn-new");
        return btn;
    }

    // ==================== SKELETON ====================
    private Component createSkeletonCard() {
        Div card = new Div();
        card.addClassName("event-card");

        Div image = new Div();
        image.addClassName("skeleton-image");

        Div content = new Div();
        content.addClassName("skeleton-content");

        Div line1 = new Div(); line1.addClassName("skeleton-line");
        Div line2 = new Div(); line2.addClassName("skeleton-line");
        Div line3 = new Div(); line3.addClassName("skeleton-line");

        content.add(line1, line2, line3);
        card.add(image, content);
        return card;
    }

    // ==================== UTILS ====================
    private String getCategoryImage(String category) {
        return switch (category.toLowerCase()) {
            case "concert" -> "/images/fistivale.jpg";
            case "théâtre", "theatre" -> "/images/theatre.jpg";
            case "conférence", "conference" -> "/images/Corporate Event Creativity - conferences.jpg";
            case "sport" -> "/images/Sport.jpg";
            default -> "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800";
        };
    }

}