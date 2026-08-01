package com.pedidos.mayorista.config;

import com.pedidos.mayorista.service.UsuarioDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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


    // ==========================
    // SECURITY
    // ==========================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

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
                // AUTORIZACIONES
                // ==========================

                .authorizeHttpRequests(auth -> auth

                        // Login y recursos públicos
                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()


                        // ==========================
                        // USUARIOS
                        // SOLO ADMINISTRADOR
                        // ==========================

                        .requestMatchers(
                                "/api/usuarios/**"
                        ).hasRole("ADMINISTRADOR")


                        // ==========================
                        // TODO LO DEMÁS
                        // REQUIERE LOGIN
                        // ==========================

                        .anyRequest().authenticated()
                )


                // ==========================
                // LOGIN
                // ==========================

                .formLogin(form -> form

                        .defaultSuccessUrl(
                                "/dashboard.html",
                                true
                        )

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