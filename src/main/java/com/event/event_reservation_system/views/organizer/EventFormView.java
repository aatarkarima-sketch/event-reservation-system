package com.event.event_reservation_system.views.organizer;

import com.event.event_reservation_system.modele.Categorie;
import com.event.event_reservation_system.modele.Event;
import com.event.event_reservation_system.modele.User;
import com.event.event_reservation_system.security.SecurityUtils;
import com.event.event_reservation_system.service.EventService;
import com.event.event_reservation_system.views.UnifiedLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDateTime;

@Route(value = "organizer/event/:action?/:id?", layout = UnifiedLayout.class)
@PageTitle("Gérer Événement | Event Manager")
@RolesAllowed({"ORGANIZER", "ADMIN"})
@CssImport("./styles/event-form-view.css")
public class EventFormView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final SecurityUtils securityUtils;

    private Event event;
    private boolean isEditMode = false;
    private Binder<Event> binder;

    private TextField titreField;
    private TextArea descriptionField;
    private ComboBox<Categorie> categorieField;
    private DateTimePicker dateDebutField;
    private DateTimePicker dateFinField;
    private TextField lieuField;
    private TextField villeField;
    private IntegerField capaciteField;
    private NumberField prixField;
    private TextField imageUrlField;

    public EventFormView(EventService eventService, SecurityUtils securityUtils) {
        this.eventService = eventService;
        this.securityUtils = securityUtils;

        addClassName("event-form-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        getStyle()
                .set("background", "linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        String action = beforeEnterEvent.getRouteParameters().get("action").orElse("new");
        String idParam = beforeEnterEvent.getRouteParameters().get("id").orElse(null);

        if ("edit".equals(action) && idParam != null) {
            try {
                Long eventId = Long.parseLong(idParam);
                event = eventService.trouverParId(eventId);
                isEditMode = true;
            } catch (Exception e) {
                Notification.show(
                        "Événement non trouvé",
                        3000,
                        Notification.Position.MIDDLE
                ).addThemeVariants(NotificationVariant.LUMO_ERROR);
                beforeEnterEvent.forwardTo("organizer/events");
                return;
            }
        } else {
            event = new Event();
            isEditMode = false;
        }

        createContent();
    }

    private void createContent() {
        removeAll();

        // Container principal
        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setMaxWidth("1000px");
        mainContainer.setWidthFull();
        mainContainer.setPadding(true);
        mainContainer.setSpacing(true);
        mainContainer.getStyle()
                .set("margin", "0 auto")
                .set("padding", "32px 24px");

        mainContainer.add(
                createHeader(),
                createFormCard(),
                createActions()
        );

        add(mainContainer);

        if (isEditMode) {
            binder.readBean(event);
        }
    }

    private Component createHeader() {
        // Icône
        Icon icon = isEditMode ? VaadinIcon.EDIT.create() : VaadinIcon.PLUS_CIRCLE.create();
        icon.setSize("48px");
        icon.getStyle()
                .set("color", "#1e40af")
                .set("margin-bottom", "12px");

        // Titre principal
        H1 title = new H1(isEditMode ? "Modifier l'événement" : "Créer un nouvel événement");
        title.getStyle()
                .set("margin", "0")
                .set("color", "#1e293b")
                .set("font-weight", "800")
                .set("font-size", "2.25rem")
                .set("text-align", "center");

        // Sous-titre
        Span subtitle = new Span(
                isEditMode ?
                        "Modifiez les informations de votre événement et enregistrez les changements" :
                        "Remplissez le formulaire ci-dessous pour créer un événement exceptionnel"
        );
        subtitle.getStyle()
                .set("color", "#64748b")
                .set("font-size", "1.125rem")
                .set("text-align", "center")
                .set("max-width", "600px")
                .set("margin", "12px auto 0");

        // Badge de statut
        Span badge = new Span(isEditMode ? "ÉDITION" : "CRÉATION");
        badge.getStyle()
                .set("background", "linear-gradient(135deg, #1e40af 0%, #3b82f6 100%)")
                .set("color", "white")
                .set("padding", "6px 16px")
                .set("border-radius", "20px")
                .set("font-weight", "600")
                .set("font-size", "0.813rem")
                .set("letter-spacing", "0.5px")
                .set("margin-top", "16px")
                .set("display", "inline-block");

        VerticalLayout header = new VerticalLayout(icon, title, subtitle, badge);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle()
                .set("text-align", "center")
                .set("padding", "32px 24px")
                .set("background", "white")
                .set("border-radius", "16px")
                .set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.05)")
                .set("margin-bottom", "24px");

        return header;
    }

    private Component createFormCard() {
        binder = new Binder<>(Event.class);

        // Titre
        titreField = new TextField("Titre de l'événement");
        titreField.setRequiredIndicatorVisible(true);
        titreField.setPlaceholder("Ex: Concert de musique Gnaoua");
        titreField.setPrefixComponent(VaadinIcon.TEXT_LABEL.create());
        styleField(titreField);

        binder.forField(titreField)
                .asRequired("Le titre est obligatoire")
                .withValidator(titre -> titre.length() >= 5, "Le titre doit contenir au moins 5 caractères")
                .withValidator(titre -> titre.length() <= 100, "Le titre ne peut pas dépasser 100 caractères")
                .bind(Event::getTitre, Event::setTitre);

        // Description
        descriptionField = new TextArea("Description");
        descriptionField.setMaxLength(1000);
        descriptionField.setHelperText("0/1000 caractères");
        descriptionField.setPlaceholder("Décrivez votre événement en détail...");
        descriptionField.setPrefixComponent(VaadinIcon.TEXT_INPUT.create());
        styleField(descriptionField);
        descriptionField.addValueChangeListener(e ->
                descriptionField.setHelperText(
                        (e.getValue() != null ? e.getValue().length() : 0) + "/1000 caractères"
                )
        );

        binder.forField(descriptionField)
                .bind(Event::getDescription, Event::setDescription);

        // Catégorie
        categorieField = new ComboBox<>("Catégorie");
        categorieField.setItems(Categorie.values());
        categorieField.setItemLabelGenerator(Categorie::getDisplayName);
        categorieField.setRequiredIndicatorVisible(true);
        categorieField.setPlaceholder("Sélectionnez une catégorie");
        categorieField.setPrefixComponent(VaadinIcon.TAG.create());
        styleField(categorieField);

        binder.forField(categorieField)
                .asRequired("La catégorie est obligatoire")
                .bind(Event::getCategorie, Event::setCategorie);

        // Date de début
        dateDebutField = new DateTimePicker("Date et heure de début");
        dateDebutField.setRequiredIndicatorVisible(true);
        dateDebutField.setMin(LocalDateTime.now());
        styleField(dateDebutField);

        binder.forField(dateDebutField)
                .asRequired("La date de début est obligatoire")
                .withValidator(date -> date.isAfter(LocalDateTime.now()),
                        "La date doit être dans le futur")
                .bind(Event::getDateDebut, Event::setDateDebut);

        // Date de fin
        dateFinField = new DateTimePicker("Date et heure de fin");
        dateFinField.setRequiredIndicatorVisible(true);
        styleField(dateFinField);

        binder.forField(dateFinField)
                .asRequired("La date de fin est obligatoire")
                .withValidator(dateFin -> {
                    LocalDateTime dateDebut = dateDebutField.getValue();
                    return dateDebut == null || dateFin.isAfter(dateDebut);
                }, "La date de fin doit être après la date de début")
                .bind(Event::getDateFin, Event::setDateFin);

        // Lieu
        lieuField = new TextField("Lieu");
        lieuField.setRequiredIndicatorVisible(true);
        lieuField.setPlaceholder("Ex: Stade Mohammed V");
        lieuField.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
        styleField(lieuField);

        binder.forField(lieuField)
                .asRequired("Le lieu est obligatoire")
                .bind(Event::getLieu, Event::setLieu);

        // Ville
        villeField = new TextField("Ville");
        villeField.setRequiredIndicatorVisible(true);
        villeField.setPlaceholder("Ex: Casablanca");
        villeField.setPrefixComponent(VaadinIcon.HOME.create());
        styleField(villeField);

        binder.forField(villeField)
                .asRequired("La ville est obligatoire")
                .bind(Event::getVille, Event::setVille);

        // Capacité maximale
        capaciteField = new IntegerField("Capacité maximale");
        capaciteField.setRequiredIndicatorVisible(true);
        capaciteField.setMin(1);
        capaciteField.setStepButtonsVisible(true);
        capaciteField.setHelperText("Nombre total de places disponibles");
        capaciteField.setPlaceholder("Ex: 500");
        capaciteField.setPrefixComponent(VaadinIcon.USERS.create());
        styleField(capaciteField);

        binder.forField(capaciteField)
                .asRequired("La capacité est obligatoire")
                .withValidator(capacite -> capacite > 0, "La capacité doit être supérieure à 0")
                .bind(Event::getCapaciteMax, Event::setCapaciteMax);

        // Prix unitaire
        prixField = new NumberField("Prix par place (DH)");
        prixField.setRequiredIndicatorVisible(true);
        prixField.setMin(0);
        prixField.setStep(10);
        prixField.setHelperText("Prix en dirhams");
        prixField.setPlaceholder("Ex: 150");
        prixField.setPrefixComponent(VaadinIcon.MONEY.create());
        styleField(prixField);

        binder.forField(prixField)
                .asRequired("Le prix est obligatoire")
                .withValidator(prix -> prix >= 0, "Le prix doit être positif ou nul")
                .bind(Event::getPrixUnitaire, Event::setPrixUnitaire);

        // Image URL (optionnel)
        imageUrlField = new TextField("URL de l'image (optionnel)");
        imageUrlField.setPlaceholder("https://example.com/image.jpg");
        imageUrlField.setPrefixComponent(VaadinIcon.PICTURE.create());
        styleField(imageUrlField);

        binder.forField(imageUrlField)
                .bind(Event::getImageUrl, Event::setImageUrl);

        // Layout du formulaire
        FormLayout formLayout = new FormLayout();
        formLayout.addClassName("event-form");

        formLayout.add(
                titreField,
                categorieField,
                descriptionField,
                dateDebutField,
                dateFinField,
                lieuField,
                villeField,
                capaciteField,
                prixField,
                imageUrlField
        );

        // Définition des colonnes occupées
        formLayout.setColspan(titreField, 2);
        formLayout.setColspan(descriptionField, 2);
        formLayout.setColspan(imageUrlField, 2);

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        formLayout.getStyle()
                .set("padding", "32px")
                .set("border-radius", "16px")
                .set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.05)")
                .set("background-color", "white")
                .set("gap", "24px");

        return formLayout;
    }

    private void styleField(Component field) {
        field.getElement().getStyle()
                .set("--lumo-contrast-10pct", "#f1f5f9")
                .set("--lumo-primary-color", "#1e40af");
    }

    private Component createActions() {
        // Bouton Annuler
        Icon cancelIcon = VaadinIcon.CLOSE.create();
        cancelIcon.getStyle().set("margin-right", "8px");

        Button cancelButton = new Button("Annuler", cancelIcon);
        cancelButton.addClassName("cancel-button");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.getStyle()
                .set("color", "#64748b")
                .set("border", "2px solid #cbd5e1")
                .set("border-radius", "8px")
                .set("padding", "12px 24px")
                .set("font-weight", "600")
                .set("transition", "all 0.2s ease");
        cancelButton.addClickListener(e ->
                cancelButton.getUI().ifPresent(ui -> ui.navigate("organizer/events"))
        );

        // Bouton Enregistrer en brouillon
        Icon draftIcon = VaadinIcon.FILE.create();
        draftIcon.getStyle().set("margin-right", "8px");

        Button saveDraftButton = new Button("Enregistrer en brouillon", draftIcon);
        saveDraftButton.addClassName("draft-button");
        saveDraftButton.getStyle()
                .set("background", "linear-gradient(135deg, #475569 0%, #64748b 100%)")
                .set("color", "white")
                .set("border", "none")
                .set("border-radius", "8px")
                .set("padding", "12px 24px")
                .set("font-weight", "600")
                .set("box-shadow", "0 4px 6px rgba(71, 85, 105, 0.3)")
                .set("transition", "all 0.2s ease");
        saveDraftButton.addClickListener(e -> saveEvent(false));

        // Bouton Enregistrer et publier
        Icon publishIcon = VaadinIcon.CHECK_CIRCLE.create();
        publishIcon.getStyle().set("margin-right", "8px");

        Button savePublishButton = new Button(
                isEditMode ? "Enregistrer les modifications" : "Enregistrer et publier",
                publishIcon
        );
        savePublishButton.addClassName("publish-button");
        savePublishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        savePublishButton.getStyle()
                .set("background", "linear-gradient(135deg, #1e40af 0%, #3b82f6 100%)")
                .set("color", "white")
                .set("border", "none")
                .set("border-radius", "8px")
                .set("padding", "12px 32px")
                .set("font-weight", "600")
                .set("box-shadow", "0 4px 6px rgba(30, 64, 175, 0.3)")
                .set("transition", "all 0.2s ease");
        savePublishButton.addClickListener(e -> saveEvent(true));

        HorizontalLayout actions = new HorizontalLayout(
                cancelButton, saveDraftButton, savePublishButton
        );
        actions.setJustifyContentMode(JustifyContentMode.END);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        actions.setWidthFull();
        actions.setSpacing(true);
        actions.getStyle()
                .set("margin-top", "24px")
                .set("padding", "24px 32px")
                .set("background", "white")
                .set("border-radius", "16px")
                .set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.05)")
                .set("gap", "16px");

        return actions;
    }

    private void saveEvent(boolean publish) {
        try {
            Event eventToSave = isEditMode ? event : new Event();
            binder.writeBean(eventToSave);

            User currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("Utilisateur non connecté"));

            if (isEditMode) {
                eventService.modifierEvenement(event.getId(), eventToSave, currentUser.getId());

                Notification.show(
                        "✓ Événement modifié avec succès",
                        3000,
                        Notification.Position.TOP_CENTER
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Event savedEvent = eventService.creerEvenement(eventToSave, currentUser.getId());

                if (publish) {
                    eventService.publierEvenement(savedEvent.getId(), currentUser.getId());
                }

                Notification.show(
                        publish ? "✓ Événement créé et publié avec succès" : "✓ Événement créé en brouillon",
                        3000,
                        Notification.Position.TOP_CENTER
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }

            getUI().ifPresent(ui -> ui.navigate("organizer/events"));

        } catch (ValidationException e) {
            Notification.show(
                    "⚠ Veuillez corriger les erreurs dans le formulaire",
                    3000,
                    Notification.Position.TOP_CENTER
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show(
                    "✗ Erreur: " + e.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}