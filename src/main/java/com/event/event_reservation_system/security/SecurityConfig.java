package com.event.event_reservation_system.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig extends VaadinWebSecurity {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/",
                        "/login",
                        "/register",
                        "/events/**",
                        "/event/**",
                        "/images/**",
                        "/frontend/**",
                        "/h2-console/**"
                ).permitAll()

                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/organizer/**").hasAnyRole("ORGANIZER", "ADMIN")
        );

        // H2 console (dev)
        http.headers(headers ->
                headers.frameOptions(frame -> frame.disable())
        );

        // Login personnalisé
        http.formLogin(form -> form
                .loginPage("/login")
                .successHandler(customSuccessHandler())
                .permitAll()
        );

        // UserDetailsService
        http.userDetailsService(customUserDetailsService);

        // ⚠️ TOUJOURS EN DERNIER
        super.configure(http);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return new SavedRequestAwareAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    Authentication authentication
            ) throws IOException, jakarta.servlet.ServletException {

                boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                boolean isOrganizer = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZER"));

                boolean isClient = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));

                if(isAdmin) {
                    response.sendRedirect("/admin/dashboard");

                }
                if (isOrganizer) {
                    response.sendRedirect("/organizer/dashboard");
                } else if (isClient) {
                    response.sendRedirect("/client-dashboard");
                } else {
                    super.onAuthenticationSuccess(request, response, authentication);
                }
            }
        };
    }
}
