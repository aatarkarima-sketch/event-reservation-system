package com.event.event_reservation_system.views.client;

import com.event.event_reservation_system.modele.Event;
import com.event.event_reservation_system.modele.Reservation;
import com.event.event_reservation_system.modele.User;
import com.event.event_reservation_system.security.SecurityUtils;
import com.event.event_reservation_system.service.EventService;
import com.event.event_reservation_system.service.ReservationService;
import com.event.event_reservation_system.views.UnifiedLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;

@Route(value = "event/:id/reserve", layout = UnifiedLayout.class)
@PageTitle("Réserver | Event Manager")
@CssImport("./styles/reservation-form-view.css")
@PermitAll
public class ReservationFormView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final ReservationService reservationService;
    private final SecurityUtils securityUtils;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Event event;
    private Long eventId;

    private IntegerField placesField;
    private TextArea commentField;
    private Span totalPriceSpan;
    private Span placesCountSpan;

    public ReservationFormView(EventService eventService,
                               ReservationService reservationService,
                               SecurityUtils securityUtils) {
        this.eventService = eventService;
        this.reservationService = reservationService;
        this.securityUtils = securityUtils;

        addClassName("reservation-form-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        try {
            eventId = Long.parseLong(
                    beforeEnterEvent.getRouteParameters().get("id").orElse("0")
            );

            event = eventService.trouverParId(eventId);

            if (!event.isDisponible()) {
                Notification.show(
                        "Cet événement n'est plus disponible pour la réservation",
                        3000,
                        Notification.Position.MIDDLE
                ).addThemeVariants(NotificationVariant.LUMO_ERROR);
                beforeEnterEvent.forwardTo("event/" + eventId);
                return;
            }

            if (event.isComplet()) {
                Notification.show(
                        "Cet événement est complet",
                        3000,
                        Notification.Position.MIDDLE
                ).addThemeVariants(NotificationVariant.LUMO_ERROR);
                beforeEnterEvent.forwardTo("event/" + eventId);
                return;
            }

            createContent();

        } catch (Exception e) {
            Notification.show(
                    "Événement non trouvé",
                    3000,
                    Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
            beforeEnterEvent.forwardTo("");
        }
    }

    private void createContent() {
        removeAll();

        add(
                createBreadcrumb(),
                createPageTitle(),
                createMainContent()
        );
    }

    private Component createBreadcrumb() {
        Div breadcrumb = new Div();
        breadcrumb.addClassName("breadcrumb");

        Anchor home = new Anchor("", "Accueil");
        home.addClassName("breadcrumb-link");

        Span separator1 = new Span("›");
        separator1.addClassName("breadcrumb-separator");

        Anchor events = new Anchor("events", "Événements");
        events.addClassName("breadcrumb-link");

        Span separator2 = new Span("›");
        separator2.addClassName("breadcrumb-separator");

        Span current = new Span("Réservation");
        current.addClassName("breadcrumb-current");

        breadcrumb.add(home, separator1, events, separator2, current);
        return breadcrumb;
    }

    private Component createPageTitle() {
        H1 title = new H1("Réserver pour " + event.getTitre());
        title.addClassName("page-title-reservation");
        return title;
    }

    private Component createMainContent() {
        Div mainContainer = new Div();
        mainContainer.addClassName("reservation-main-container");

        mainContainer.add(
                createEventHeroImage(),
                createReservationCard()
        );

        return mainContainer;
    }

    private Component createEventHeroImage() {
        Div heroContainer = new Div();
        heroContainer.addClassName("hero-image-container");

        Image heroImage = new Image(
                event.getImageUrl() != null ? event.getImageUrl() : getDefaultImage(),
                event.getTitre()
        );
        heroImage.addClassName("hero-image");

        Div overlay = new Div();
        overlay.addClassName("hero-overlay");

        H2 eventTitle = new H2(event.getTitre());
        eventTitle.addClassName("hero-title");

        overlay.add(eventTitle);
        heroContainer.add(heroImage, overlay);

        return heroContainer;
    }

    private Component createReservationCard() {
        Div card = new Div();
        card.addClassName("reservation-card");

        card.add(
                createCardTitle(),
                createPlacesSection(),
                createAvailabilityInfo(),
                createCommentSection(),
                createRecapitulatif(),
                createActionButtons()
        );

        return card;
    }

    private Component createCardTitle() {
        H3 title = new H3("Réserver pour " + event.getTitre());
        title.addClassName("card-title");
        return title;
    }

    private Component createPlacesSection() {
        Div section = new Div();
        section.addClassName("places-section");

        // Label
        Span label = new Span("Nombre de places");
        label.addClassName("field-label");

        // Contrôles de places avec prix
        HorizontalLayout controlsLayout = new HorizontalLayout();
        controlsLayout.addClassName("places-controls-layout");

        // Partie gauche: contrôles
        Div controlsWrapper = new Div();
        controlsWrapper.addClassName("places-controls");

        Button minusBtn = new Button(VaadinIcon.MINUS.create());
        minusBtn.addClassName("places-button");
        minusBtn.addClickListener(e -> {
            Integer current = placesField.getValue();
            if (current != null && current > 1) {
                placesField.setValue(current - 1);
            }
        });

        placesField = new IntegerField();
        placesField.setValue(1);
        placesField.setMin(1);
        placesField.setMax(Math.min(10, event.getPlacesDisponibles()));
        placesField.addClassName("places-input");
        placesField.addValueChangeListener(e -> updateTotalPrice());

        Button plusBtn = new Button(VaadinIcon.PLUS.create());
        plusBtn.addClassName("places-button");
        plusBtn.addClickListener(e -> {
            Integer current = placesField.getValue();
            if (current != null && current < Math.min(10, event.getPlacesDisponibles())) {
                placesField.setValue(current + 1);
            }
        });

        Icon chevron = VaadinIcon.CHEVRON_RIGHT.create();
        chevron.addClassName("places-chevron");

        controlsWrapper.add(minusBtn, placesField, plusBtn, chevron);

        // Partie droite: prix
        Div pricesWrapper = new Div();
        pricesWrapper.addClassName("prices-wrapper");

        Div priceRow = new Div();
        priceRow.addClassName("price-row");
        Span priceLabel = new Span("Prix unitaire:");
        priceLabel.addClassName("price-label");
        Span priceValue = new Span(String.format("%.0f€", event.getPrixUnitaire()));
        priceValue.addClassName("price-value");
        priceRow.add(priceLabel, priceValue);

        Div totalRow = new Div();
        totalRow.addClassName("total-row");
        Span totalLabel = new Span("Montant total:");
        totalLabel.addClassName("total-label");
        totalPriceSpan = new Span(String.format("%.0f€", event.getPrixUnitaire()));
        totalPriceSpan.addClassName("total-value");
        totalRow.add(totalLabel, totalPriceSpan);

        pricesWrapper.add(priceRow, totalRow);

        controlsLayout.add(controlsWrapper, pricesWrapper);

        section.add(label, controlsLayout);
        return section;
    }

    private Component createAvailabilityInfo() {
        Div info = new Div();
        info.addClassName("availability-info");

        Icon checkIcon = VaadinIcon.CHECK_CIRCLE.create();
        checkIcon.addClassName("availability-icon");

        Span text = new Span(event.getPlacesDisponibles() + " places sont disponibles.");
        text.addClassName("availability-text");

        info.add(checkIcon, text);
        return info;
    }

    private Component createCommentSection() {
        Div section = new Div();
        section.addClassName("comment-section");

        Span label = new Span("Commentaire ");
        label.addClassName("field-label");

        Span optional = new Span("(optionnel)");
        optional.addClassName("optional-text");

        Div labelWrapper = new Div(label, optional);
        labelWrapper.addClassName("label-wrapper");

        commentField = new TextArea();
        commentField.setPlaceholder("Ajouter un commentaire...");
        commentField.setMaxLength(500);
        commentField.addClassName("comment-textarea");

        section.add(labelWrapper, commentField);
        return section;
    }

    private Component createRecapitulatif() {
        Div recap = new Div();
        recap.addClassName("recap-section");

        H4 title = new H4("Récapitulatif");
        title.addClassName("recap-title");

        Div row1 = createRecapRow("Nombre de places:", "1");
        placesCountSpan = (Span) row1.getChildren()
                .filter(component -> component instanceof Span)
                .skip(1)
                .findFirst()
                .orElse(new Span("1"));

        Div row2 = createRecapRow("Prix unitaire:", String.format("%.0f€", event.getPrixUnitaire()));

        Div totalRow = new Div();
        totalRow.addClassName("recap-total-row");
        Span totalLabel = new Span("Montant total:");
        totalLabel.addClassName("recap-total-label");
        Span totalValue = new Span(String.format("%.0f€", event.getPrixUnitaire()));
        totalValue.addClassName("recap-total-value");
        totalRow.add(totalLabel, totalValue);

        recap.add(title, row1, row2, totalRow);
        return recap;
    }

    private Div createRecapRow(String label, String value) {
        Div row = new Div();
        row.addClassName("recap-row");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("recap-label");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("recap-value");

        row.add(labelSpan, valueSpan);
        return row;
    }

    private Component createActionButtons() {
        Div buttonsContainer = new Div();
        buttonsContainer.addClassName("action-buttons");

        Button confirmButton = new Button("Confirmer la réservation");
        confirmButton.addClassName("confirm-button");
        confirmButton.addClickListener(e -> confirmReservation());

        Span notice = new Span("Vous devez être connecté pour réserver.");
        notice.addClassName("connection-notice");

        buttonsContainer.add(confirmButton, notice);
        return buttonsContainer;
    }

    private void updateTotalPrice() {
        Integer places = placesField.getValue();
        if (places != null && places > 0) {
            double total = places * event.getPrixUnitaire();
            totalPriceSpan.setText(String.format("%.0f€", total));

            if (placesCountSpan != null) {
                placesCountSpan.setText(String.valueOf(places));
            }

            // Mettre à jour aussi dans le récapitulatif
            getElement().executeJs(
                    "const recapRows = document.querySelectorAll('.recap-row');" +
                            "if (recapRows.length > 0) recapRows[0].querySelector('.recap-value').textContent = $0;" +
                            "const totalRow = document.querySelector('.recap-total-row');" +
                            "if (totalRow) totalRow.querySelector('.recap-total-value').textContent = $1;",
                    String.valueOf(places),
                    String.format("%.0f€", total)
            );
        }
    }

    private void confirmReservation() {
        try {
            Integer places = placesField.getValue();
            if (places == null || places < 1) {
                Notification.show(
                        "Veuillez sélectionner au moins 1 place",
                        3000,
                        Notification.Position.MIDDLE
                ).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (places > event.getPlacesDisponibles()) {
                Notification.show(
                        "Il n'y a que " + event.getPlacesDisponibles() + " places disponibles",
                        3000,
                        Notification.Position.MIDDLE
                ).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            User currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("Utilisateur non connecté"));

            Reservation reservation = reservationService.creerReservation(
                    currentUser.getId(),
                    eventId,
                    places,
                    commentField.getValue()
            );

            reservationService.confirmerReservation(reservation.getId());

            showSuccessDialog(reservation.getCodeReservation());

        } catch (Exception e) {
            Notification.show(
                    "Erreur lors de la réservation: " + e.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void showSuccessDialog(String codeReservation) {
        Div dialog = new Div();
        dialog.addClassName("success-dialog");

        Icon successIcon = VaadinIcon.CHECK_CIRCLE.create();
        successIcon.addClassName("success-icon");

        H3 title = new H3("Réservation confirmée !");
        title.addClassName("success-title");

        Paragraph message = new Paragraph(
                "Votre réservation a été confirmée avec succès. Votre code de réservation est :"
        );
        message.addClassName("success-message");

        Div codeBox = new Div();
        codeBox.addClassName("reservation-code-box");
        Span code = new Span(codeReservation);
        code.addClassName("reservation-code");
        codeBox.add(code);

        Button okButton = new Button("OK");
        okButton.addClassName("success-ok-button");
        okButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("my-reservations"))
        );

        dialog.add(successIcon, title, message, codeBox, okButton);

        removeAll();
        add(dialog);
    }

    private String getDefaultImage() {
        return switch (event.getCategorie()) {
            case CONCERT -> "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=800&h=400&fit=crop";
            case THEATRE -> "https://images.unsplash.com/photo-1503095396549-807759245b35?w=800&h=400&fit=crop";
            case CONFERENCE -> "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&h=400&fit=crop";
            case SPORT -> "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=800&h=400&fit=crop";
            default -> "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800&h=400&fit=crop";
        };
    }
}