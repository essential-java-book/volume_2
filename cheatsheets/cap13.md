# Cheat sheet · Capítulo 13 — Autenticación con Spring Security

*Java Esencial · Volumen 2: Spring Boot · código del capítulo: tag `v2-cap13`*

## En una frase

Sabes proteger la API con Spring Security: decides qué rutas son públicas y cuáles exigen un rol, cargas los usuarios desde la base de datos y guardas las contraseñas como hash BCrypt. La autenticación es HTTP Basic sin estado, la base sobre la que se monta JWT en el capítulo siguiente.

## Lo esencial

| Elemento | Para qué sirve |
|---|---|
| `spring-boot-starter-security` | Al añadirlo, todos los endpoints quedan protegidos: usuario `user` y contraseña aleatoria que aparece en el log de arranque |
| `spring-security-test` (scope `test`) | Permite simular usuarios (`@WithMockUser(roles = "ADMIN")`) y tokens CSRF (`.with(csrf())`) en los tests de `@WebMvcTest`, que ahora llevan `@Import(SeguridadConfig.class)` |
| `SecurityFilterChain` | Bean central: rutas públicas, rutas protegidas, mecanismo de autenticación y CSRF |
| `@EnableWebSecurity` | Activa la configuración personalizada, en `SeguridadConfig` (paquete `configuracion`) |
| `.csrf(csrf -> csrf.disable())` | Desactiva CSRF: en una API REST sin estado no aplica |
| `authorizeHttpRequests` | Reglas de acceso por ruta y método HTTP; van de la más específica a la más genérica |
| `permitAll()` / `authenticated()` | Ruta sin autenticación / ruta que exige credenciales válidas |
| `hasRole("ADMIN")` / `hasAnyRole("ADMIN", "USER")` | La ruta exige ese rol (`ROLE_ADMIN`) o cualquiera de los indicados |
| `.httpBasic(basic -> {})` | Activa HTTP Basic: `Authorization: Basic base64(usuario:contraseña)` en cada petición |
| `SessionCreationPolicy.STATELESS` | Sin sesión HTTP: cada petición lleva sus propias credenciales |
| `frameOptions(frame -> frame.sameOrigin())` | Deja que la consola H2, que usa un iframe, siga funcionando |
| `UserDetailsService` | Interfaz con un único método, `loadUserByUsername`, que devuelve un `UserDetails` (nombre, hash y roles) |
| `BCryptPasswordEncoder` | `encode()` genera un hash lento y con sal aleatoria; `matches()` compara texto plano con hash |
| `DaoAuthenticationProvider` | Conecta el `UserDetailsService` (carga el usuario) con el `PasswordEncoder` (verifica la contraseña) |

## Código mínimo

Las reglas de `SeguridadConfig`: lectura pública, escritura solo para `ADMIN`, préstamos para `ADMIN` o `USER`.

```java
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
```

`BibliotecaUserDetailsService` traduce una `Credencial` a `UserDetails`. El rol se guarda sin prefijo (`ADMIN`, `USER`): `roles(...)` añade `ROLE_` por ti.

```java
@Override
public UserDetails loadUserByUsername(String username) {
    Credencial credencial = credenciales.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

    return User.builder()
            .username(credencial.getUsername())
            .password(credencial.getPassword())
            .roles(credencial.getRol())
            .build();
}
```

## Comandos y peticiones

```http
### GET público, sin credenciales
GET http://localhost:8080/libros

### POST protegido con credenciales correctas -> 201 Created
POST http://localhost:8080/libros
Content-Type: application/json
Authorization: Basic YWRtaW46YWRtaW4xMjM=

{"titulo": "El Quijote", "autor": "Cervantes", "anio": 1605}

### En IntelliJ puedes escribir usuario y contraseña sin codificar
POST http://localhost:8080/libros
Content-Type: application/json
Authorization: Basic admin admin123

{"titulo": "El Quijote", "autor": "Cervantes", "anio": 1605}
```

## Errores típicos

- **`401 Unauthorized` con usuario y contraseña correctos** → en `data.sql` hay una contraseña en texto plano. Guarda siempre el hash: `new BCryptPasswordEncoder().encode("admin123")`.
- **`403 Forbidden` en POST, PUT, PATCH y DELETE con credenciales válidas** → CSRF sigue activo. Añade `.csrf(csrf -> csrf.disable())`.
- **La consola H2 sale en blanco o da 403** → faltan las dos cosas: `requestMatchers("/h2-console/**").permitAll()` y `frameOptions(frame -> frame.sameOrigin())`.
- **`NoSuchBeanDefinitionException` de `UserDetailsService` al arrancar** → a `BibliotecaUserDetailsService` le falta `@Service` o está fuera de `com.javaesencial.biblioteca`.
- **El formulario `/login` redirige en bucle con `STATELESS`** → es lo esperado: sin sesión no hay login por formulario. En una API REST las credenciales viajan en la cabecera de cada petición.

## En el Proyecto Biblioteca

Entran `Credencial` y `CredencialJpaRepository` (cuenta de acceso a la API, independiente de `Usuario`), `BibliotecaUserDetailsService` en el paquete nuevo `seguridad` y `SeguridadConfig` en `configuracion`. La migración `V3__crear_tabla_credencial.sql` crea la tabla y `data.sql` inserta `admin` (rol `ADMIN`, contraseña `admin123`) y `bibliotecario` (rol `USER`, contraseña `user123`) con sus hashes BCrypt. `LibroControllerTest` se adapta a la seguridad. Quedan pendientes JWT (Capítulo 14) y Actuator + Docker (Capítulo 15).
