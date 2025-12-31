package com.event.event_reservation_system.views.client;

import com.event.event_reservation_system.modele.User;
import com.event.event_reservation_system.security.SecurityUtils;
import com.event.event_reservation_system.service.UserService;
import com.event.event_reservation_system.views.MainLayout;
import com.event.event_reservation_system.views.UnifiedLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Route(value = "profile", layout = UnifiedLayout.class)
@PageTitle("Mon Profil | Event Manager")
@CssImport("./styles/profile-view.css")
@PermitAll
public class ProfileView extends VerticalLayout {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    private User currentUser;
    private Binder<User> binder;

    private TextField nomField;
    private TextField prenomField;
    private EmailField emailField;
    private TextField telephoneField;

    private PasswordField currentPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;

    public ProfileView(UserService userService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.securityUtils = securityUtils;

        currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Utilisateur non connecté"));

        addClassName("profile-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);

        // Container principal
        VerticalLayout container = new VerticalLayout();
        container.addClassName("profile-container");
        container.setWidthFull();
        container.setMaxWidth("1000px");
        container.setPadding(true);
        container.setSpacing(false);
        container.add(
                createHeroSection(),
                createProfileFormCard(),
                createStatsCard(),
                createPasswordCard(),
                createDangerZone()
        );

        add(container);
    }

    private Component createHeroSection() {
        Avatar avatar = new Avatar(
                currentUser.getPrenom() + " " + currentUser.getNom()
        );        avatar.addClassName("hero-avatar");
        avatar.setColorIndex(Math.abs(currentUser.getEmail().hashCode()) % 8);

        H1 title = new H1(currentUser.getPrenom() + " " + currentUser.getNom());
        title.addClassName("hero-title");

        Span roleBadge = new Span(currentUser.getRole().getLabel());
        roleBadge.addClassName("role-badge");
        roleBadge.getStyle()
                .set("background-color", currentUser.getRole().getColor())
                .set("color", "white");

        Div meta1 = createMetaItem(VaadinIcon.ENVELOPE, currentUser.getEmail());
        Div meta2 = createMetaItem(VaadinIcon.CALENDAR,
                "Membre depuis " + currentUser.getDateInscription()
                        .format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        VerticalLayout metaLayout = new VerticalLayout(meta1, meta2);
        metaLayout.addClassName("hero-meta");
        metaLayout.setSpacing(false);

        VerticalLayout content = new VerticalLayout(title, roleBadge, metaLayout);
        content.setAlignItems(Alignment.CENTER);
        content.addClassName("hero-content");

        VerticalLayout hero = new VerticalLayout(avatar, content);
        hero.addClassName("profile-hero");
        hero.setAlignItems(Alignment.CENTER);
        hero.setSpacing(true);

        return hero;
    }


    private Component createProfileInfoCard() {
        // Avatar
        Avatar avatar = new Avatar(currentUser.getPrenom() + " " + currentUser.getNom());
        avatar.addClassName("profile-avatar");
        avatar.setColorIndex(Math.abs(currentUser.getEmail().hashCode()) % 8);

        Div avatarSection = new Div(avatar);
        avatarSection.addClassName("profile-avatar-section");

        // Détails utilisateur
        H3 userName = new H3(currentUser.getPrenom() + " " + currentUser.getNom());
        userName.addClassName("profile-name");

        Span roleBadge = new Span(currentUser.getRole().getLabel());
        roleBadge.addClassName("role-badge");
        roleBadge.getStyle()
                .set("background-color", currentUser.getRole().getColor())
                .set("color", "white");

        // Meta informations
        Div emailItem = createMetaItem(VaadinIcon.ENVELOPE, currentUser.getEmail());
        Div dateItem = createMetaItem(
                VaadinIcon.CALENDAR,
                "Membre depuis " + currentUser.getDateInscription()
                        .format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        );

        VerticalLayout metaInfo = new VerticalLayout(emailItem, dateItem);
        metaInfo.addClassName("profile-meta");
        metaInfo.setPadding(false);
        metaInfo.setSpacing(false);

        VerticalLayout detailsLayout = new VerticalLayout(userName, roleBadge, metaInfo);
        detailsLayout.addClassName("profile-details");
        detailsLayout.setPadding(false);

        // Card layout
        Div infoCard = new Div();
        infoCard.addClassName("profile-card");
        infoCard.addClassName("profile-info-card");

        infoCard.add(avatarSection, detailsLayout);

        return infoCard;
    }

    private Div createMetaItem(VaadinIcon icon, String text) {
        Icon itemIcon = icon.create();
        itemIcon.addClassName("meta-icon");
        Span itemText = new Span(text);
        Div item = new Div(itemIcon, itemText);
        item.addClassName("meta-item");
        return item;
    }

    private Component createProfileFormCard() {
        H3 title = new H3("Informations personnelles");
        title.addClassName("card-title");

        binder = new Binder<>(User.class);

        nomField = new TextField("Nom");
        prenomField = new TextField("Prénom");
        emailField = new EmailField("Email");
        telephoneField = new TextField("Téléphone");
        telephoneField.setHelperText("Optionnel");

        // Préfix icons + required
        nomField.setPrefixComponent(VaadinIcon.USER.create());
        prenomField.setPrefixComponent(VaadinIcon.USER.create());
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        telephoneField.setPrefixComponent(VaadinIcon.PHONE.create());

        nomField.setRequiredIndicatorVisible(true);
        prenomField.setRequiredIndicatorVisible(true);
        emailField.setRequiredIndicatorVisible(true);

        // Binder bindings...
        binder.forField(nomField).asRequired().bind(User::getNom, User::setNom);
        binder.forField(prenomField).asRequired().bind(User::getPrenom, User::setPrenom);
        binder.forField(emailField).asRequired().bind(User::getEmail, User::setEmail);
        binder.forField(telephoneField).bind(User::getTelephone, User::setTelephone);
        binder.readBean(currentUser);

        FormLayout formLayout = new FormLayout(nomField, prenomField, emailField, telephoneField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        Button saveButton = new Button("Enregistrer", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveProfile());

        Div card = new Div(title, formLayout, saveButton);
        card.addClassName("profile-card");
        return card;
    }

    private void saveProfile() {
        try {
            User updatedUser = new User();
            binder.writeBean(updatedUser);

            userService.mettreAJourProfil(currentUser.getId(), updatedUser);

            // Recharger l'utilisateur
            currentUser = userService.trouverParId(currentUser.getId());

            Notification notification = Notification.show(
                    "✓ Profil mis à jour avec succès",
                    3000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (ValidationException e) {
            Notification notification = Notification.show(
                    "⚠ Veuillez corriger les erreurs dans le formulaire",
                    3000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification notification = Notification.show(
                    "✗ Erreur lors de la mise à jour: " + e.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private Component createStatsCard() {
        H3 title = new H3("Mes statistiques");
        title.addClassName("card-title");

        Map<String, Object> stats = userService.getStatistiquesUtilisateur(currentUser.getId());

        Div res = createStatItem(VaadinIcon.TICKET, stats.get("nombreReservations").toString(), "Réservations", "#667eea");
        Div spent = createStatItem(VaadinIcon.DOLLAR, String.format("%.2f DH", stats.get("montantTotalDepense")), "Total dépensé", "#10b981");
        Div events = createStatItem(VaadinIcon.CALENDAR, "5", "Événements à venir", "#f59e0b");

        Div grid = new Div(res, spent, events);
        grid.addClassName("stats-bento");

        Div card = new Div(title, grid);
        card.addClassName("profile-card");
        return card;
    }



    private Div createStatItem(VaadinIcon icon, String value, String label, String color) {
        // Simplifié et moderne
        Icon i = icon.create();
        i.getStyle().set("color", color);
        H4 val = new H4(value);
        val.getStyle().set("color", color);
        Span lab = new Span(label);

        Div item = new Div(i, val, lab);
        item.addClassName("stat-item");
        return item;
    }
    private Component createPasswordCard() {
        H3 title = new H3("Changer le mot de passe");
        title.addClassName("card-title");

        // Requirements info
        Div requirements = new Div();
        requirements.addClassName("password-requirements");

        H4 reqTitle = new H4("Exigences du mot de passe");
        reqTitle.addClassName("password-requirements-title");

        UnorderedList reqList = new UnorderedList();
        reqList.addClassName("password-requirements-list");
        reqList.add(
                new ListItem("Au moins 8 caractères"),
                new ListItem("Une lettre majuscule"),
                new ListItem("Une lettre minuscule"),
                new ListItem("Un chiffre")
        );

        requirements.add(reqTitle, reqList);

        currentPasswordField = new PasswordField("Mot de passe actuel");
        currentPasswordField.setRequiredIndicatorVisible(true);
        currentPasswordField.setPrefixComponent(VaadinIcon.LOCK.create());

        newPasswordField = new PasswordField("Nouveau mot de passe");
        newPasswordField.setRequiredIndicatorVisible(true);
        newPasswordField.setPrefixComponent(VaadinIcon.KEY.create());

        confirmPasswordField = new PasswordField("Confirmer le nouveau mot de passe");
        confirmPasswordField.setRequiredIndicatorVisible(true);
        confirmPasswordField.setPrefixComponent(VaadinIcon.KEY.create());

        Div passwordForm = new Div(currentPasswordField, newPasswordField, confirmPasswordField);
        passwordForm.addClassName("password-form");

        Button changePasswordButton = new Button("Changer le mot de passe", VaadinIcon.SHIELD.create());
        changePasswordButton.addClassName("profile-button-primary");
        changePasswordButton.addClickListener(e -> changePassword());

        Div card = new Div(title, requirements, passwordForm, changePasswordButton);

        card.addClassName("profile-card");

        return card;
    }

    private void changePassword() {
        String currentPassword = currentPasswordField.getValue();
        String newPassword = newPasswordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();

        // Validations
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Notification notification = Notification.show(
                    "⚠ Tous les champs sont obligatoires",
                    3000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Notification notification = Notification.show(
                    "⚠ Les mots de passe ne correspondent pas",
                    3000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            userService.changerMotDePasse(currentUser.getId(), currentPassword, newPassword);

            // Nettoyer les champs
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();

            Notification notification = Notification.show(
                    "✓ Mot de passe changé avec succès",
                    3000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            Notification notification = Notification.show(
                    "✗ Erreur: " + e.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private Component createDangerZone() {
        H3 title = new H3("⚠ Zone dangereuse");
        title.addClassName("card-title");

        Div warningBox = new Div();
        warningBox.addClassName("danger-warning");

        Span warningIcon = new Span("⚠️");
        warningIcon.addClassName("danger-warning-icon");

        Paragraph warningText = new Paragraph(
                "La désactivation de votre compte est une action irréversible. " +
                        "Vous perdrez l'accès à toutes vos réservations et données."
        );
        warningText.addClassName("danger-warning-text");

        warningBox.add(warningIcon, warningText);

        Button deactivateButton = new Button("Désactiver mon compte", VaadinIcon.TRASH.create());
        deactivateButton.addClassName("profile-button-danger");
        deactivateButton.addClickListener(e -> confirmDeactivation());

        Div dangerCard = new Div(title, warningBox, deactivateButton);
        dangerCard.addClassName("danger-zone");

        return dangerCard;
    }

    private void confirmDeactivation() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("⚠ Désactiver le compte");
        dialog.setText(
                "Êtes-vous vraiment sûr de vouloir désactiver votre compte ?\n\n" +
                        "Cette action est irréversible et vous perdrez l'accès à toutes vos réservations."
        );

        dialog.setCancelable(true);
        dialog.setCancelText("Annuler");
        dialog.setConfirmText("Oui, désactiver");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> deactivateAccount());

        dialog.open();
    }

    private void deactivateAccount() {
        try {
            userService.desactiverCompte(currentUser.getId());

            Notification notification = Notification.show(
                    "Compte désactivé. Vous allez être déconnecté.",
                    3000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Déconnexion
            securityUtils.logout();
            getUI().ifPresent(ui -> {
                ui.navigate("login");
                ui.getPage().reload();
            });

        } catch (Exception e) {
            Notification notification = Notification.show(
                    "✗ Erreur lors de la désactivation: " + e.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}