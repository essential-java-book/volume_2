# Práctica 13.4 — Cabecera WWW-Authenticate personalizada

## Enunciado

Al devolver un 401, Spring Security incluye la cabecera `WWW-Authenticate: Basic
realm="Realm"`. Personaliza el valor del realm a `"Biblioteca API"` configurando un
`BasicAuthenticationEntryPoint` en `SecurityFilterChain`.

## Solución

`SeguridadConfig` con el `BasicAuthenticationEntryPoint` añadido:

```java
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
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

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
    public BasicAuthenticationEntryPoint entryPoint() {
        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("Biblioteca API");
        return entryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, BasicAuthenticationEntryPoint entryPoint)
            throws Exception {
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
                .httpBasic(basic -> basic.authenticationEntryPoint(entryPoint));

        return http.build();
    }
}
```

Los dos cambios respecto a la versión del cuerpo del capítulo son: el bean nuevo
`entryPoint()` (con `setRealmName("Biblioteca API")`) y, en `filterChain`, sustituir
`.httpBasic(basic -> {})` —la configuración por defecto, sin personalizar nada— por
`.httpBasic(basic -> basic.authenticationEntryPoint(entryPoint))`, pasando el bean nuevo
como parámetro del método (Spring lo inyecta automáticamente, igual que ya hace con
`HttpSecurity`).

### Qué es un `AuthenticationEntryPoint`

Es el componente que Spring Security invoca cuando una petición no autenticada llega a un
recurso protegido: decide *cómo* pedirle credenciales al cliente. Con HTTP Basic, eso
significa fijar el código `401` y añadir la cabecera `WWW-Authenticate`, cuyo valor le dice
al cliente (o al navegador, si mostrara un diálogo de usuario/contraseña) qué "reino" de
protección está pidiendo autenticarse — un dato puramente informativo para quien recibe la
respuesta, sin ningún efecto sobre la lógica de autenticación o autorización.
`BasicAuthenticationEntryPoint` es la implementación que Spring Security usa por defecto
para HTTP Basic; sin personalizar nada, su `realmName` vale `"Realm"` (el valor genérico
del enunciado), y `setRealmName(...)` es el único método que hace falta llamar para
cambiarlo.

### Comprobación

```terminal
GET /prestamos
```
(sin cabecera `Authorization`)

```terminal
HTTP/1.1 401
WWW-Authenticate: Basic realm="Biblioteca API"
```

Antes de esta práctica, la misma petición devolvía `WWW-Authenticate: Basic realm="Realm"`
— el resto de la respuesta (código `401`, sin cuerpo) no cambia; solo el nombre del reino
en la cabecera.
