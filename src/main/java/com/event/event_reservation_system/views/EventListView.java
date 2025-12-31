package com.event.event_reservation_system.views;

import com.event.event_reservation_system.dto.EventDTO;
import com.event.event_reservation_system.modele.Categorie;
import com.event.event_reservation_system.service.EventService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "events", layout = UnifiedLayout.class)
@PageTitle("Événements | EventManager")
@CssImport("./styles/eventlist-view.css")
@AnonymousAllowed
public class EventListView extends VerticalLayout implements BeforeEnterObserver {
    private final EventService eventService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private TextField searchField;
    private ComboBox<Categorie> categoryFilter;
    private ComboBox<String> villeFilter;
    private DatePicker startDateFilter;
    private DatePicker endDateFilter;
    private NumberField minPriceFilter;
    private NumberField maxPriceFilter;
    private Div eventsContainer;
    private H1 mainTitle;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 3;
    private int totalEvents = 0;
    private Div paginationContainer;

    public EventListView(EventService eventService) {
        this.eventService = eventService;
        setSizeFull();
        addClassName("events-list-modern");
        setPadding(false);
        setSpacing(false);
        setAlignItems(FlexComponent.Alignment.CENTER); // Centrage global
        // Construction de la page
        add(createMainTitle(),
                createFiltersSection(),
                createEventsGrid(),
                createPagination()
        );
        updateEventsList();
    }

    // ----------------- Titre principal -----------------
    // Version moderne avec sous-titre et classe animée
    private Component createMainTitle() {
        Div titleWrapper = new Div();
        titleWrapper.getStyle().set("text-align", "center");
        titleWrapper.setWidthFull();

        mainTitle = new H1("Découvrez tous les événements disponibles");
        mainTitle.addClassName("page-main-title-modern");

        Paragraph subTitle = new Paragraph("Filtrez, explorez et trouvez l'événement parfait pour vous !");
        subTitle.addClassName("page-subtitle-modern");

        titleWrapper.add(mainTitle, subTitle);
        return titleWrapper;
    }


    // ----------------- Section filtres -----------------
    private Component createFiltersSection() {
        Div container = new Div();
        container.addClassName("filters-section-wrapper");

        H2 filtersTitle = new H2("Filtres avancés");
        filtersTitle.addClassName("filters-title");

        // Ligne 1
        HorizontalLayout row1 = new HorizontalLayout();
        row1.addClassName("filters-row");

        // Catégorie
        Div categoryWrapper = new Div();
        categoryWrapper.addClassName("filter-group");
        Span categoryLabel = new Span("Catégorie:");
        categoryLabel.addClassName("filter-label");
        categoryFilter = new ComboBox<>();
        categoryFilter.setItems(Categorie.values());
        categoryFilter.setItemLabelGenerator(Categorie::getLabel);
        categoryFilter.setPlaceholder("Toutes catégories");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setPrefixComponent(VaadinIcon.TAGS.create());
        categoryFilter.addValueChangeListener(e -> resetAndUpdate());
        categoryFilter.addClassName("filter-input");
        categoryWrapper.add(categoryLabel, categoryFilter);

        // Plage de dates
        Div dateRangeWrapper = new Div();
        dateRangeWrapper.addClassNames("filter-group", "date-range-group");
        Span dateLabel = new Span("Plage de dates:");
        dateLabel.addClassName("filter-label");
        HorizontalLayout dateRange = new HorizontalLayout();
        dateRange.addClassName("date-range");
        startDateFilter = new DatePicker("Du");
        startDateFilter.setPrefixComponent(VaadinIcon.CALENDAR.create());
        startDateFilter.addClassNames("filter-input", "date-input");
        startDateFilter.addValueChangeListener(e -> resetAndUpdate());
        Span arrow = new Span("→");
        arrow.addClassName("date-arrow");
        endDateFilter = new DatePicker("Au");
        endDateFilter.setPrefixComponent(VaadinIcon.CALENDAR.create());
        endDateFilter.addClassNames("filter-input", "date-input");
        endDateFilter.addValueChangeListener(e -> resetAndUpdate());
        dateRange.add(startDateFilter, arrow, endDateFilter);
        dateRangeWrapper.add(dateLabel, dateRange);
        row1.add(categoryWrapper, dateRangeWrapper);

        // Ligne 2
        HorizontalLayout row2 = new HorizontalLayout();
        row2.addClassName("filters-row");

        // Ville
        Div villeWrapper = new Div();
        villeWrapper.addClassName("filter-group");
        Span villeLabel = new Span("Ville:");
        villeLabel.addClassName("filter-label");
        villeFilter = new ComboBox<>();
        villeFilter.setItems(eventService.getToutesLesVilles());
        villeFilter.setPlaceholder("Saisir une ville");
        villeFilter.setClearButtonVisible(true);
        villeFilter.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
        villeFilter.addValueChangeListener(e -> resetAndUpdate());
        villeFilter.addClassName("filter-input");
        villeWrapper.add(villeLabel, villeFilter);

        // Plage de prix
        Div priceRangeWrapper = new Div();
        priceRangeWrapper.addClassNames("filter-group", "price-range-group");
        Span priceLabel = new Span("Plage de prix:");
        priceLabel.addClassName("filter-label");
        HorizontalLayout priceRange = new HorizontalLayout();
        priceRange.addClassName("price-range");
        minPriceFilter = new NumberField();
        minPriceFilter.setPlaceholder("€ Min");
        minPriceFilter.setPrefixComponent(VaadinIcon.EURO.create());
        minPriceFilter.addClassNames("filter-input", "price-input");
        minPriceFilter.addValueChangeListener(e -> resetAndUpdate());
        maxPriceFilter = new NumberField();
        maxPriceFilter.setPlaceholder("€ Max");
        maxPriceFilter.setPrefixComponent(VaadinIcon.EURO.create());
        maxPriceFilter.addClassNames("filter-input", "price-input");
        maxPriceFilter.addValueChangeListener(e -> resetAndUpdate());
        priceRange.add(minPriceFilter, maxPriceFilter);
        priceRangeWrapper.add(priceLabel, priceRange);
        row2.add(villeWrapper, priceRangeWrapper);

        // Ligne 3
        HorizontalLayout row3 = new HorizontalLayout();
        row3.addClassName("filters-row");

        // Mot-clé
        Div keywordWrapper = new Div();
        keywordWrapper.addClassName("filter-group");
        Span keywordLabel = new Span("Mot-clé:");
        keywordLabel.addClassName("filter-label");
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher...");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addValueChangeListener(e -> resetAndUpdate());
        searchField.addClassNames("filter-input", "keyword-input");
        keywordWrapper.add(keywordLabel, searchField);

        // Boutons
        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.addClassName("action-buttons");
        Button filterBtn = new Button("Filtrer", VaadinIcon.FILTER.create());
        filterBtn.addClassName("btn-filter");
        filterBtn.addClickListener(e -> updateEventsList());
        Button resetBtn = new Button("Réinitialiser", VaadinIcon.REFRESH.create());
        resetBtn.addClassName("btn-reset");
        resetBtn.addClickListener(e -> {
            categoryFilter.clear();
            villeFilter.clear();
            startDateFilter.clear();
            endDateFilter.clear();
            minPriceFilter.clear();
            maxPriceFilter.clear();
            searchField.clear();
            resetAndUpdate();
        });
        ComboBox<String> sortBy = new ComboBox<>();
        sortBy.setItems("Date", "Prix", "Popularité");
        sortBy.setPlaceholder("Trier par");
        sortBy.setPrefixComponent(VaadinIcon.ARROW_DOWN.create());
        sortBy.addClassNames("filter-input", "sort-select");
        actionButtons.add(filterBtn, resetBtn, sortBy);
        row3.add(keywordWrapper, actionButtons);

        container.add(filtersTitle, row1, row2, row3);
        return container;
    }

    // ----------------- Grille des événements -----------------
    private Component createEventsGrid() {
        Div wrapper = new Div();
        wrapper.addClassName("events-grid-wrapper");
        eventsContainer = new Div();
        eventsContainer.addClassName("events-grid");
        wrapper.add(eventsContainer);
        return wrapper;
    }

    // ----------------- Pagination -----------------
    private Component createPagination() {
        paginationContainer = new Div();
        paginationContainer.addClassName("pagination-modern");
        return paginationContainer;
    }

    // ----------------- Mise à jour des événements -----------------
    private void updateEventsList() {
        eventsContainer.removeAll();
        String keyword = searchField.getValue() != null ? searchField.getValue().trim() : "";
        Categorie cat = categoryFilter.getValue();
        String ville = villeFilter.getValue();
        LocalDate start = startDateFilter.getValue();
        LocalDate end = endDateFilter.getValue();
        Double minPrice = minPriceFilter.getValue();
        Double maxPrice = maxPriceFilter.getValue();
        LocalDateTime startDateTime = start != null ? start.atStartOfDay() : null;
        LocalDateTime endDateTime = end != null ? end.atTime(23, 59, 59) : null;
        List<EventDTO> events = eventService.rechercherEvenementsDTO(
                cat, null, ville, minPrice, maxPrice, startDateTime, endDateTime, keyword
        );
        events = events.stream().filter(EventDTO::isDisponible).toList();
        totalEvents = events.size();
        int startIndex = currentPage * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, totalEvents);
        if (events.isEmpty()) {
            eventsContainer.add(createEmptyState());
        } else {
            events.subList(startIndex, endIndex)
                    .forEach(event -> eventsContainer.add(createEventCard(event)));
        }
        updatePagination();
    }

    private void updatePagination() {
        paginationContainer.removeAll();
        int totalPages = (int) Math.ceil((double) totalEvents / PAGE_SIZE);
        if (totalPages <= 1) return;
        HorizontalLayout pagination = new HorizontalLayout();
        pagination.addClassName("pagination-controls");
        pagination.setAlignItems(FlexComponent.Alignment.CENTER);
        // Bouton Page précédente
        Button prevPage = new Button("Page " + (currentPage + 1) + " sur " + totalPages);
        prevPage.addClassName("page-info");
        prevPage.setIcon(VaadinIcon.ARROW_LEFT.create());
        prevPage.setEnabled(currentPage > 0);
        prevPage.addClickListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateEventsList();
            }
        });
        // Numéros de page
        Div pageNumbers = new Div();
        pageNumbers.addClassName("page-numbers-modern");
        int startPage = Math.max(0, currentPage - 2);
        int endPage = Math.min(totalPages - 1, currentPage + 2);
        for (int i = startPage; i <= endPage; i++) {
            int pageNum = i;
            Button pageBtn = new Button(String.valueOf(i + 1));
            pageBtn.addClassName("page-number-btn");
            if (i == currentPage) {
                pageBtn.addClassName("active");
            }
            pageBtn.addClickListener(e -> {
                currentPage = pageNum;
                updateEventsList();
            });
            pageNumbers.add(pageBtn);
        }
        if (endPage < totalPages - 1) {
            Span dots = new Span("...");
            dots.addClassName("page-dots");
            pageNumbers.add(dots);
            Button lastPage = new Button(String.valueOf(totalPages));
            lastPage.addClassName("page-number-btn");
            lastPage.addClickListener(e -> {
                currentPage = totalPages - 1;
                updateEventsList();
            });
            pageNumbers.add(lastPage);
        }
        // Bouton Suivant
        Button nextPage = new Button("Suivant");
        nextPage.addClassName("btn-next");
        nextPage.setIconAfterText(true);
        nextPage.setIcon(VaadinIcon.ARROW_RIGHT.create());
        nextPage.setEnabled((currentPage + 1) < totalPages);
        nextPage.addClickListener(e -> {
            if ((currentPage + 1) < totalPages) {
                currentPage++;
                updateEventsList();
            }
        });
        pagination.add(prevPage, pageNumbers, nextPage);
        paginationContainer.add(pagination);
    }

    private void resetAndUpdate() {
        currentPage = 0;
        updateEventsList();
    }

    private Component createEventCard(EventDTO event) {
        Div card = new Div();
        card.addClassName("event-card");

        // Image en haut (pleine largeur)
        Div imgWrapper = new Div();
        imgWrapper.addClassName("event-img");
        Image img = new Image(
                event.getImageUrl() != null ? event.getImageUrl() : getFallbackImage(event.getCategorie()),
                event.getTitre()
        );
        img.addClassName("event-image");
        imgWrapper.add(img);

        // Contenu en bas
        Div content = new Div();
        content.addClassName("event-content");
        H3 title = new H3(event.getTitre());
        title.addClassName("event-title");
        Paragraph location = new Paragraph(event.getVille() + " | " + event.getDateDebut().format(dateFormatter));
        location.addClassName("event-location");
        Div details = new Div();
        details.addClassName("event-details");
        details.add(
                new Paragraph("• " + event.getCategorie().getLabel()),
                new Paragraph("• Prix: " + String.format("%.0f€", event.getPrixUnitaire()))
        );
        Button detailsBtn = new Button("Voir détails");
        detailsBtn.addClassName("btn-details");
        detailsBtn.addClickListener(e -> {
            System.out.println("Tentative de navigation vers event/" + event.getId());
            try {
                UI.getCurrent().navigate("details/" + event.getId());
                System.out.println("Navigation lancée");
            } catch (Exception ex) {
                System.err.println("Erreur de navigation: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        content.add(title, location, details, detailsBtn);

        card.add(imgWrapper, content); // Layout vertical
        return card;
    }

    private Component createEmptyState() {
        Div empty = new Div();
        empty.addClassName("empty-state");
        empty.add(
                new H3("Aucun événement trouvé"),
                new Paragraph("Essayez de modifier vos critères de recherche")
        );
        return empty;
    }

    private String getFallbackImage(Categorie cat) {
        return switch (cat) {
            case CONCERT -> "/images/Corporate Event Creativity - conferences.jpg";
            case THEATRE -> "/images/theatre.jpg";
            case CONFERENCE -> "/images/fistivale.jpg";
            case SPORT -> "/images/sportevent.jpg";
            default -> "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800";
        };
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        updateEventsList();
    }
}