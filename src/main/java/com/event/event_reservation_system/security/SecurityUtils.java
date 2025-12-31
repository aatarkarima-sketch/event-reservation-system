package com.event.event_reservation_system.security;


import com.event.event_reservation_system.Repositories.UserRepository;
import com.event.event_reservation_system.modele.User;
import com.vaadin.flow.server.VaadinServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtils {
    private final UserRepository userRepository;

    /**
     * Récupère l'utilisateur actuellement connecté
     */
    public Optional<User> getCurrentUser() {
        return getAuthenticatedUsername()
                .flatMap(userRepository::findByEmail);
    }

    /**
     * Récupère le username (email) de l'utilisateur connecté
     */
    public Optional<String> getAuthenticatedUsername() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        return Optional.of(authentication.getName());
    }

    /**
     * Vérifie si un utilisateur est connecté
     */
    public boolean isUserLoggedIn() {
        return getAuthenticatedUsername().isPresent();
    }

    /**
     * Vérifie si l'utilisateur connecté a un rôle spécifique
     */
    public boolean hasRole(String role) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Vérifie si l'utilisateur est admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Vérifie si l'utilisateur est organisateur
     */
    public boolean isOrganizer() {
        return hasRole("ORGANIZER") || hasRole("ADMIN");
    }

    /**
     * Déconnexion de l'utilisateur
     */
    public void logout() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(),
                null,
                null
        );
    }
}
