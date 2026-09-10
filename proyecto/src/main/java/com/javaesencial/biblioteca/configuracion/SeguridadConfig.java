package com.javaesencial.biblioteca.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Autenticación HTTP Basic y autorización por rol. El Capítulo 14 la
 * reescribe para JWT (sin {@code httpBasic}, con {@code JwtFiltro}),
 * pero esta clase se queda siempre en {@code configuracion/} -- nunca
 * se muda a {@code seguridad/}.
 *
 * Nota sobre este repositorio: en el manuscrito las reglas por rol
 * solo aparecen como práctica opcional (13.1); aquí se implementan
 * directamente para que el estado final del volumen (tabla de
 * endpoints del Capítulo 15) sea coherente sin depender de un
 * ejercicio no incluido en este repositorio.
 */
@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                              PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/libros", "/libros/**").permitAll()
                        .requestMatchers("/docs/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/libros").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/libros/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/libros/**").hasRole("ADMIN")
                        .requestMatchers("/prestamos/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated())
                .httpBasic(basic -> {
                });

        return http.build();
    }
}
