package com.event.event_reservation_system.views.admin;

import com.event.event_reservation_system.modele.*;
import com.event.event_reservation_system.service.EventService;
import com.event.event_reservation_system.service.ReservationService;
import com.event.event_reservation_system.service.UserService;
import com.event.event_reservation_system.views.UnifiedLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.model.*;
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
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.component.charts.Chart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Route(value = "admin/dashboard", layout = UnifiedLayout.class)
@PageTitle("Tableau de bord Administrateur | Evently")
@CssImport("./styles/admin-dashboard-view.css")
@RolesAllowed("ADMIN")
@AnonymousAllowed // Si tu utilises un guard personnalisé, sinon retire cette ligne
public class AdminDashboardView extends VerticalLayout {

    private final UserService userService;
    private final EventService eventService;
    private final ReservationService reservationService;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", java.util.Locale.FRENCH);

    public AdminDashboardView(UserService userService,
                              EventService eventService,
                              ReservationService reservationService) {
        this.userService = userService;
        this.eventService = eventService;
        this.reservationService = reservationService;

        addClassName("admin-dashboard-view");
        setPadding(false);
        setSpacing(false);
        setSizeFull();

        buildDashboard();
    }

    private void buildDashboard() {
        add(createBreadcrumb());
        add(createPageTitle());
        add(createStatsCards());
        add(createChartsSection());
        add(createRecentActivitySection());
        add(createBottomStats());
    }

    private Component createBreadcrumb() {
        Div container = new Div();
        container.addClassName("breadcrumb-container");

        Anchor home = new Anchor("", "Accueil");
        home.addClassName("breadcrumb-link");

        Span sep = new Span(" › ");
        sep.addClassName("breadcrumb-separator");

        Span current = new Span("Tableau de bord Administrateur");
        current.addClassName("breadcrumb-current");

        container.add(home, sep, current);

        // Centrage du breadcrumb
        container.getStyle()
                .set("text-align", "center")
                .set("justify-content", "center");
        return container;
    }

    private H1 createPageTitle() {
        H1 title = new H1("Tableau de bord Administrateur");
        title.addClassName("page-title");
        title.getStyle()
                .set("text-align", "center")  // Centrage du texte
                .set("width", "100%")         // Prend toute la largeur pour centrer correctement
                .set("margin", "40px 0");
        return title;
    }

    private Component createStatsCards() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addClassName("stats-cards");
        layout.setWidthFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        long totalUsers = userService.compterTous();
        long totalEvents = eventService.compterTous();
        long totalReservations = reservationService.countAllConfirmed();
        BigDecimal totalRevenue = reservationService.calculateTotalRevenue();

        Map<String, Long> usersByRole = userService.getUsersByRoleCount();
        Map<Statut, Long> eventsByStatus = eventService.getEventsCountByStatus();

        layout.add(
                createMainStatCard(VaadinIcon.USERS, "Utilisateurs Totaux", String.valueOf(totalUsers), createRoleBreakdown(usersByRole)),
                createMainStatCard(VaadinIcon.CALENDAR, "Événements Totaux", String.valueOf(totalEvents), createStatusBreakdown(eventsByStatus)),
                createMainStatCard(VaadinIcon.TICKET, "Réservations Confirmées", String.valueOf(totalReservations), null),
                createMainStatCard(VaadinIcon.EURO, "Revenus Totaux", totalRevenue + " €", null)
        );

        return layout;
    }

    private VerticalLayout createMainStatCard(VaadinIcon icon, String title, String value, Component breakdown) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("stat-card-modern");

        // Icône avec fond très léger
        Icon iconElement = icon.create();
        iconElement.addClassName("stat-icon-modern");

        H4 titleH = new H4(title); // H4 au lieu de H3 pour plus de légèreté
        titleH.addClassName("stat-title");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("stat-value-modern");

        card.add(iconElement, titleH, valueSpan);
        if (breakdown != null) {
            breakdown.addClassName("stat-breakdown-modern");
            card.add(breakdown);
        }

        return card;
    }

    private Component createRoleBreakdown(Map<String, Long> counts) {
        VerticalLayout breakdown = new VerticalLayout();
        breakdown.addClassName("stat-breakdown");
        breakdown.add(new Span(counts.getOrDefault("CLIENT", 0L) + " Clients"));
        breakdown.add(new Span(counts.getOrDefault("ORGANIZER", 0L) + " Organisateurs"));
        breakdown.add(new Span(counts.getOrDefault("ADMIN", 0L) + " Administrateurs"));
        return breakdown;
    }

    private Component createStatusBreakdown(Map<Statut, Long> counts) {
        VerticalLayout breakdown = new VerticalLayout();
        breakdown.addClassName("stat-breakdown");
        breakdown.add(new Span(counts.getOrDefault(Statut.PUBLIE, 0L) + " Publiés"));
        breakdown.add(new Span(counts.getOrDefault(Statut.BROUILLON, 0L) + " Brouillon"));
        breakdown.add(new Span(counts.getOrDefault(Statut.TERMINE, 0L) + " Terminés"));
        breakdown.add(new Span(counts.getOrDefault(Statut.ANNULE, 0L) + " Annulés"));
        return breakdown;
    }

    private Component createChartsSection() {
        HorizontalLayout section = new HorizontalLayout();
        section.addClassName("reservations-chart-section");
        section.setWidthFull();
        section.setSpacing(true);

        // Graphique 1 : Réservations par mois (Line Chart) - Données réelles !
        Chart lineChart = new Chart(ChartType.LINE);
        lineChart.addClassName("reservations-line-chart");
        lineChart.setHeight("400px");
        lineChart.setWidth("100%");

        Configuration lineConf = lineChart.getConfiguration();
        lineConf.setTitle("Réservations par mois (" + LocalDateTime.now().getYear() + ")");
        lineConf.getTooltip().setEnabled(true);
        lineConf.getTooltip().setValueSuffix(" réservations");

        XAxis xAxis = new XAxis();
        xAxis.setCategories("Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sept", "Oct", "Nov", "Déc");
        lineConf.addxAxis(xAxis);

        YAxis yAxis = new YAxis();
        yAxis.setTitle("Nombre de réservations");
        yAxis.setMin(0);
        lineConf.addyAxis(yAxis);

// Récupérer les vraies données
        Map<String, Long> reservationsParMois = reservationService.getReservationsParMois();

        ListSeries series = new ListSeries("Réservations confirmées");
        reservationsParMois.values().forEach(value -> series.addData(value.intValue()));
        lineConf.addSeries(series);



        // Graphique 2 : Répartition utilisateurs par rôle (Pie Chart)
        Chart pieChart = new Chart(ChartType.PIE);
        pieChart.addClassName("user-role-pie");
        pieChart.setHeight("400px");
        pieChart.setWidth("100%");

        Configuration pieConf = pieChart.getConfiguration();
        pieConf.setTitle("Répartition utilisateurs par rôle");

        DataSeries pieSeries = new DataSeries();
        Map<String, Long> usersByRole = userService.getUsersByRoleCount();
        pieSeries.add(new DataSeriesItem("Clients", usersByRole.getOrDefault("CLIENT", 0L)));
        pieSeries.add(new DataSeriesItem("Organisateurs", usersByRole.getOrDefault("ORGANIZER", 0L)));
        pieSeries.add(new DataSeriesItem("Administrateurs", usersByRole.getOrDefault("ADMIN", 0L)));

        PlotOptionsPie pieOptions = new PlotOptionsPie();
        pieOptions.setInnerSize("50%"); // Donut style
        pieOptions.setAllowPointSelect(true);
        pieOptions.setCursor(Cursor.POINTER);
        pieOptions.getDataLabels().setEnabled(true);
        pieSeries.setPlotOptions(pieOptions);

        pieConf.addSeries(pieSeries);

        // Graphique 3 : Événements par statut (Column Chart)
        Chart columnChart = new Chart(ChartType.COLUMN);
        columnChart.addClassName("events-status-bar-chart");
        columnChart.setHeight("400px");
        columnChart.setWidth("100%");

        Configuration columnConf = columnChart.getConfiguration();
        columnConf.setTitle("Événements par statut");

        DataSeries columnSeries = new DataSeries();
        Map<Statut, Long> eventsByStatus = eventService.getEventsCountByStatus();
        columnSeries.add(new DataSeriesItem("Publiés", eventsByStatus.getOrDefault(Statut.PUBLIE, 0L)));
        columnSeries.add(new DataSeriesItem("Brouillon", eventsByStatus.getOrDefault(Statut.BROUILLON, 0L)));
        columnSeries.add(new DataSeriesItem("Terminés", eventsByStatus.getOrDefault(Statut.TERMINE, 0L)));
        columnSeries.add(new DataSeriesItem("Annulés", eventsByStatus.getOrDefault(Statut.ANNULE, 0L)));

        PlotOptionsColumn columnOptions = new PlotOptionsColumn();
        columnOptions.setColorByPoint(true);
        columnSeries.setPlotOptions(columnOptions);

        columnConf.addSeries(columnSeries);

        // Ajouter les 3 graphiques dans la section (ou change le layout si tu veux 2 seulement)
        section.add(lineChart, pieChart, columnChart);

        return section;
    }

    private Component createRecentActivitySection() {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("recent-activity-section");

        H3 title = new H3("Activité Récente");
        section.add(title);

        List<Reservation> recent = reservationService.getRecentReservations(5);
        for (Reservation r : recent) {
            String timeAgo = formatTimeAgo(r.getDateReservation()); // ← Correction ici
            String desc = "Nouvelle réservation de " + r.getNombrePlaces() + " place(s) pour \"" + r.getEvenement().getTitre() + "\"";
            String amount = r.getMontantTotal() + "€";
            String statusLabel = r.getStatut().getLabel(); // Assure-toi que getLabel() existe sur StatutReservation

            VaadinIcon icon = switch (r.getStatut()) {
                case CONFIRMEE -> VaadinIcon.CHECK_CIRCLE;
                case EN_ATTENTE -> VaadinIcon.CLOCK;
                case ANNULEE -> VaadinIcon.CLOSE_CIRCLE;
                default -> VaadinIcon.INFO_CIRCLE;
            };

            section.add(createActivityItem(timeAgo, desc, amount, statusLabel, icon));
        }

        return section;
    }

    private HorizontalLayout createActivityItem(String time, String desc, String amount, String status, VaadinIcon icon) {
        HorizontalLayout item = new HorizontalLayout();
        item.addClassName("activity-item");
        item.setAlignItems(Alignment.CENTER);

        item.add(icon.create());
        item.add(new Span(time));
        VerticalLayout text = new VerticalLayout(new Span(desc), new Span(amount));
        text.setSpacing(false);
        item.add(text);

        Span statusSpan = new Span(status);
        statusSpan.addClassName("activity-status-" + status.toLowerCase().replace(" ", "-"));
        item.add(statusSpan);

        return item;
    }

    private Component createBottomStats() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addClassName("bottom-stats");

        double conversionRate = reservationService.calculateConversionRate();
        BigDecimal monthlyRevenue = reservationService.calculateMonthlyRevenue();

        layout.add(createBottomStat("Taux de Conversion", String.format("%.1f%%", conversionRate), "+3.2% ce mois-ci", true));
        layout.add(createBottomStat("Revenu ce mois-ci", monthlyRevenue + " €", "+15% vs mois précédent", true));

        return layout;
    }

    private VerticalLayout createBottomStat(String title, String value, String change, boolean positive) {
        VerticalLayout stat = new VerticalLayout();
        stat.addClassName("bottom-stat");

        H4 h4 = new H4(title);
        Span val = new Span(value);
        val.addClassName("stat-value");
        Span chg = new Span(change);
        chg.addClassName(positive ? "positive-change" : "negative-change");

        stat.add(h4, val, chg);
        return stat;
    }

    private String formatTimeAgo(LocalDateTime date) {
        // Simplifié – tu peux améliorer avec java.time.Duration
        return "Il y a quelques heures";
    }
}