package com.event.event_reservation_system.views;


import com.event.event_reservation_system.modele.User;
import com.event.event_reservation_system.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("register")
@PageTitle("Inscription | EventManager")
@AnonymousAllowed
@CssImport("./styles/register-split.css")


public class RegisterView extends VerticalLayout {

    private final UserService userService;
    private final Binder<User> binder = new Binder<>(User.class);

    private TextField prenomField;
    private TextField nomField;
    private EmailField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField telephoneField;

    public RegisterView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        addClassName("register-view-modern");

        // Fond doux
        getElement().getStyle()
                .set("background", "linear-gradient(135deg, #A7EBF2 0%, #54ACBF 100%)")
                .set("background-attachment", "fixed");

        Div card = new Div();
        card.addClassName("register-card");

        // Titre premium
        H1 title = new H1("Créer un compte");
        title.addClassName("register-title");

        Paragraph subtitle = new Paragraph("Rejoignez EventManager et réservez vos événements préférés");
        subtitle.addClassName("register-subtitle");

        // Champs
        prenomField = new TextField("Prénom");
        nomField = new TextField("Nom");
        emailField = new EmailField("Email");
        passwordField = new PasswordField("Mot de passe");
        confirmPasswordField = new PasswordField("Confirmer le mot de passe");
        telephoneField = new TextField("Téléphone (facultatif)");

        prenomField.addClassName("register-input");
        nomField.addClassName("register-input");
        emailField.addClassName("register-input");
        passwordField.addClassName("register-input");
        confirmPasswordField.addClassName("register-input");
        telephoneField.addClassName("register-input");

        // Icônes
        prenomField.setPrefixComponent(VaadinIcon.USER.create());
        nomField.setPrefixComponent(VaadinIcon.USER.create());
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        passwordField.setPrefixComponent(VaadinIcon.LOCK.create());
        confirmPasswordField.setPrefixComponent(VaadinIcon.LOCK.create());
        telephoneField.setPrefixComponent(VaadinIcon.PHONE.create());

        // Binder
        binder.forField(prenomField).asRequired("Le prénom est requis").bind(User::getPrenom, User::setPrenom);
        binder.forField(nomField).asRequired("Le nom est requis").bind(User::getNom, User::setNom);
        binder.forField(emailField).asRequired("Email requis").bind(User::getEmail, User::setEmail);
        binder.forField(passwordField).asRequired()
                .withValidator(this::validatePassword, "8+ caractères, majuscule, minuscule, chiffre")
                .bind(User::getPassword, User::setPassword);
        binder.forField(telephoneField).bind(User::getTelephone, User::setTelephone);

        // Vérification mot de passe identique
        confirmPasswordField.addValueChangeListener(e -> {
            boolean match = passwordField.getValue().equals(e.getValue());
            confirmPasswordField.setInvalid(!match);
            confirmPasswordField.setErrorMessage(match ? "" : "Les mots de passe ne correspondent pas");
        });

        // Bouton principal – petit et élégant
        Button registerBtn = new Button("S'inscrire", e -> register());
        registerBtn.addClassName("register-btn");

        // Lien connexion
        RouterLink loginLink = new RouterLink("Déjà un compte ? Se connecter", LoginView.class);
        loginLink.addClassName("login-link");

        // Formulaire
        FormLayout form = new FormLayout();
        form.addClassName("register-form-grid");
        // Ajout des champs par deux sur chaque ligne
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),    // mobile : 1 colonne
                new FormLayout.ResponsiveStep("600px", 2) // desktop : 2 colonnes
        );
        form.add(prenomField, nomField);
        form.add(emailField);
        form.setColspan(emailField, 2);
        form.add(passwordField, confirmPasswordField);
        form.add(telephoneField); // téléphone sur toute la largeur
        form.setColspan(telephoneField, 2);


        VerticalLayout content = new VerticalLayout(title, subtitle, form, registerBtn, loginLink);
        content.setAlignItems(Alignment.CENTER);
        content.setSpacing(true);
        content.setPadding(false);
        content.setMaxWidth("500px");

        card.add(content);
        add(card);
        setHorizontalComponentAlignment(Alignment.CENTER, card);
        getStyle().set("display", "flex");
        getStyle().set("flex-direction", "column");
        getStyle().set("justify-content", "center"); // Centre vertical
        getStyle().set("align-items", "center");     // Centre horizontal
    }

    private void register() {
        if (binder.validate().isOk() && passwordField.getValue().equals(confirmPasswordField.getValue())) {
            try {
                User user = new User();
                binder.writeBean(user);
                userService.inscrire(user);
                Notification.show("Inscription réussie !", 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate(LoginView.class);
            } catch (Exception ex) {
                Notification.show("Erreur : " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            Notification.show("Veuillez corriger les erreurs", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private boolean validatePassword(String pwd) {
        return pwd != null && pwd.length() >= 8 &&
                pwd.matches(".*[A-Z].*") && pwd.matches(".*[a-z].*") && pwd.matches(".*\\d.*");
    }
}