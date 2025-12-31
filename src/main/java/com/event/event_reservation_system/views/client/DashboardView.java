package com.event.event_reservation_system.views.client;

import com.event.event_reservation_system.modele.Reservation;
import com.event.event_reservation_system.modele.User;
import com.event.event_reservation_system.security.SecurityUtils;
import com.event.event_reservation_system.service.ReservationService;
import com.event.event_reservation_system.service.UserService;
import com.event.event_reservation_system.views.UnifiedLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Route(value = "client-dashboard", layout = UnifiedLayout.class)
@PageTitle("Mon espace | Event Manager")
@CssImport("./styles/dashboard-client.css")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final UserService userService;
    private final ReservationService reservationService;
    private final SecurityUtils securityUtils;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH);
    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("HH:mm");

    public DashboardView(UserService userService,
                         ReservationService reservationService,
                         SecurityUtils securityUtils) {

        this.userService = userService;
        this.reservationService = reservationService;
        this.securityUtils = securityUtils;

        User user = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Utilisateur non connecté"));

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("dashboard-root");

        add(
                createHero(user),
                createInsights(user),
                createActionCenter(),
                createMainContent(user)
        );
    }

    /* ================= HERO ================= */
    private Component createHero(User user) {

        Div hero = new Div();
        hero.addClassName("hero-pro");

        Span hello = new Span("Votre tableau de bord personnel ");
        hello.addClassName("hero-hello");

        H1 name = new H1(user.getPrenom() + " " + user.getNom());
        name.addClassName("hero-name");

        Span meta = new Span(
                "Aujourd’hui • " +
                        LocalDate.now().format(
                                DateTimeFormatter.ofPattern("EEEE dd MMMM", Locale.FRENCH))
        );
        meta.addClassName("hero-meta");

        Button cta = new Button("Explorer les événements", VaadinIcon.ARROW_RIGHT.create());
        cta.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cta.addClassName("hero-cta");
        cta.addClickListener(e ->
                cta.getUI().ifPresent(ui -> ui.navigate("events"))
        );

        VerticalLayout content = new VerticalLayout(hello, name, meta, cta);
        content.setWidthFull();
        content.setAlignItems(Alignment.CENTER);
        content.addClassName("hero-content");
        hero.setWidthFull();

        hero.add(content);
        return hero;
    }

    /* ================= INSIGHTS ================= */
    private Component createInsights(User user) {

        Map<String, Object> stats =
                userService.getStatistiquesUtilisateur(user.getId());

        Div grid = new Div(
                createInsight("Réservations",
                        stats.get("nombreReservations").toString(),
                        "au total"),
                createInsight("Événements à venir",
                        String.valueOf(
                                reservationService
                                        .getReservationsAVenir(user.getId()).size()),
                        "programmés"),
                createInsight("Total dépensé",
                        stats.get("montantTotalDepense") + " DH",
                        "cumulé")
        );

        grid.addClassName("insights-grid");
        grid.setWidthFull();
        return grid;
    }

    private Div createInsight(String title, String value, String subtitle) {

        Span t = new Span(title);
        t.addClassName("insight-title");

        Span v = new Span(value);
        v.addClassName("insight-value");

        Span s = new Span(subtitle);
        s.addClassName("insight-sub");

        Div card = new Div(t, v, s);
        card.addClassName("insight-card");
        return card;
    }

    /* ================= ACTION CENTER ================= */
    private Component createActionCenter() {

        Div bar = new Div(
                action("Mes réservations", VaadinIcon.CALENDAR, "my-reservations"),
                action("Événements", VaadinIcon.STAR, "events"),
                action("Profil", VaadinIcon.USER, "profile")
        );

        bar.addClassName("action-center");
        bar.setWidthFull();
        return bar;
    }

    private Div action(String label, VaadinIcon icon, String route) {

        Icon i = icon.create();
        Span t = new Span(label);

        Div pill = new Div(i, t);
        pill.addClassName("action-pill");
        pill.addClickListener(e ->
                pill.getUI().ifPresent(ui -> ui.navigate(route))
        );
        return pill;
    }

    /* ================= MAIN CONTENT ================= */
    private Component createMainContent(User user) {

        HorizontalLayout grid = new HorizontalLayout(
                createTimeline(user),
                createActivityFeed(user)
        );

        grid.setWidthFull();
        grid.addClassName("main-grid");
        grid.setWidthFull();
        grid.setHeightFull();
        return grid;
    }

    /* ================= TIMELINE EVENTS ================= */
    private Component createTimeline(User user) {

        Div box = new Div();
        box.addClassName("card");

        H3 title = new H3("À venir");
        title.addClassName("card-title");

        VerticalLayout list = new VerticalLayout();
        list.setPadding(false);
        list.setSpacing(false);
        list.setWidthFull();

        List<Reservation> upcoming =
                reservationService.getReservationsAVenir(user.getId());

        if (upcoming.isEmpty()) {
            list.add(new Span("Aucun événement prévu"));
        } else {
            upcoming.stream().limit(6).forEach(r -> {

                Div item = new Div(
                        new Span(
                                r.getEvenement().getDateDebut().format(timeFormatter)),
                        new Span(r.getEvenement().getTitre())
                );
                item.addClassName("timeline-item");

                item.addClickListener(e ->
                        item.getUI().ifPresent(ui ->
                                ui.navigate("event/" +
                                        r.getEvenement().getId()))
                );

                list.add(item);
            });
        }

        box.add(title, list);
        return box;
    }

    /* ================= ACTIVITY FEED ================= */
    private Component createActivityFeed(User user) {

        Div box = new Div();
        box.addClassName("card");

        H3 title = new H3("Activité récente");
        title.addClassName("card-title");

        VerticalLayout list = new VerticalLayout();
        list.setPadding(false);
        list.setSpacing(false);
        list.setWidthFull();
        reservationService.getReservationsUtilisateur(user.getId())
                .stream().limit(6)
                .forEach(r -> {

                    Div item = new Div(
                            VaadinIcon.CHECK_CIRCLE.create(),
                            new Span("Réservation confirmée"),
                            new Span("• " + r.getEvenement().getTitre())
                    );

                    item.addClassName("activity-item");
                    list.add(item);
                });

        box.add(title, list);
        return box;
    }
}
