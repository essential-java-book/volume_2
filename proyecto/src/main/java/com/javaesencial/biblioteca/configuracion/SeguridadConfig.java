package com.javaesencial.biblioteca.configuracion;

import com.javaesencial.biblioteca.seguridad.JwtFiltro;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Desde el Capítulo 14: sin {@code httpBasic}, autenticación JWT
 * stateless con {@code JwtFiltro} antes del filtro estándar de
 * usuario/contraseña. Sigue viviendo en {@code configuracion/}
 * -- nunca se muda a {@code seguridad/} (ahí solo van
 * {@code JwtServicio}, {@code JwtFiltro}, {@code AuthController},
 * los DTOs de login y {@code BibliotecaUserDetailsService}).
 *
 * <p>Desde el Capítulo 15: {@code /actuator/health} e
 * {@code /actuator/info} son públicos (los usa el healthcheck de
 * Docker); el resto de {@code /actuator/**} (p. ej. {@code metrics})
 * exige {@code ROLE_ADMIN}.
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
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFiltro jwtFiltro) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/libros", "/libros/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/docs/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/libros").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/libros/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/libros/**").hasRole("ADMIN")
                        .requestMatchers("/prestamos/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
