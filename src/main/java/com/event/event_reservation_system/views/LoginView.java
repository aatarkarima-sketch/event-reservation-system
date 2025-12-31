package com.event.event_reservation_system.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Connexion | EventManager")
@AnonymousAllowed
@CssImport(value = "./styles/login-view.css", themeFor = "vaadin-login-form")
@CssImport(value = "./styles/login-view.css", themeFor = "vaadin-login-form-wrapper")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        setSizeFull();
        addClassName("login-view-final");

        // Fond festival
        getElement().getStyle()
                .set("background", "linear-gradient(rgba(0,0,0,0.6), rgba(0,0,0,0.75)), " +
                        "url('/images/event-hero.jpg")
                .set("background-size", "cover")
                .set("background-position", "center")
                .set("background-attachment", "fixed");

        // === CARTE BLEU PREMIUM ===
        Div card = new Div();
        card.addClassName("login-card-blue");

        H1 title = new H1("EventManager");
        title.addClassName("login-title");

        Paragraph subtitle = new Paragraph("Connectez-vous pour réserver vos événements préférés");
        subtitle.addClassName("login-subtitle");

        // === UTILISE LE VRAI LoginForm DE VAADIN (compatible Spring Security) ===
        loginForm.setAction("login"); // Très important !
        loginForm.setForgotPasswordButtonVisible(false);

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle("");
        i18n.getForm().setUsername("Email");
        i18n.getForm().setPassword("Mot de passe");
        i18n.getForm().setSubmit("Se connecter");
        i18n.getErrorMessage().setTitle("Identifiants incorrects");
        i18n.getErrorMessage().setMessage("Vérifiez votre email et mot de passe");
        loginForm.setI18n(i18n);

        loginForm.addClassName("custom-login-form");

        // Lien inscription
        RouterLink registerLink = new RouterLink("Pas de compte ? S'inscrire", RegisterView.class);
        registerLink.addClassName("register-link");

        VerticalLayout content = new VerticalLayout(title, subtitle, loginForm, registerLink);
        content.setAlignItems(Alignment.CENTER);
        content.setMaxWidth("420px");
        content.setPadding(false);
        content.setSpacing(true);

        card.add(content);
        add(card);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Affiche l'erreur si ?error dans l'URL
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }
}