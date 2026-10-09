package com.pedidos.mayorista.config;

import com.pedidos.mayorista.security.ComercioActivoFilter;
import com.pedidos.mayorista.service.UsuarioDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;


    // ==========================
    // PASSWORD ENCODER
    // ==========================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }


    // El filtro de comercio suspendido corre solo dentro de la cadena de Spring Security,
    // no como filtro suelto del servlet (evita que se ejecute dos veces)
    @Bean
    public FilterRegistrationBean<ComercioActivoFilter> comercioActivoFilterRegistration(
            ComercioActivoFilter filter) {

        FilterRegistrationBean<ComercioActivoFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);

        return registration;
    }


    // ==========================
    // SECURITY
    // ==========================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ComercioActivoFilter comercioActivoFilter) throws Exception {

        http

                // ==========================
                // CSRF
                // ==========================

                .csrf(csrf -> csrf.disable())


                // ==========================
                // USUARIO DESDE BASE DE DATOS
                // ==========================

                .userDetailsService(usuarioDetailsService)


                // ==========================
                // COMERCIO SUSPENDIDO
                // ==========================

                .addFilterBefore(comercioActivoFilter, AuthorizationFilter.class)


                // ==========================
                // AUTORIZACIONES
                // ==========================

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/login",
                                "/error",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // Cualquier usuario logueado puede ver sus propios datos
                        .requestMatchers(
                                "/api/usuarios/me"
                        ).authenticated()

                        // Panel del dueño de la plataforma: alta y administración de comercios
                        .requestMatchers(
                                "/api/superadmin/**",
                                "/superadmin.html"
                        ).hasRole("SUPER_ADMIN")

                        // El administrador de cada comercio administra SUS usuarios
                        .requestMatchers(
                                "/api/usuarios/**",
                                "/usuarios.html"
                        ).hasRole("ADMINISTRADOR")

                        // Todo el resto es la operación del comercio
                        .anyRequest().hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                )


                // ==========================
                // LOGIN
                // ==========================

                .formLogin(form -> form

                        // Cada rol aterriza en su pantalla
                        .successHandler((request, response, authentication) -> {

                            boolean superAdmin = authentication.getAuthorities()
                                    .stream()
                                    .anyMatch(a ->
                                            a.getAuthority().equals("ROLE_SUPER_ADMIN"));

                            response.sendRedirect(
                                    superAdmin
                                            ? "/superadmin.html"
                                            : "/dashboard.html"
                            );
                        })

                        .permitAll()
                )


                // ==========================
                // LOGOUT
                // ==========================

                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                );


        return http.build();
    }
}
