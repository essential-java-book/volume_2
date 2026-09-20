# Práctica 13.2 — Endpoint de registro de credenciales

## Enunciado

Crea el endpoint `POST /auth/registro` que reciba `{ "username": "...", "password": "..." }`
y guarde la nueva credencial con el rol `ROLE_USER` y la contraseña hasheada con
`BCryptPasswordEncoder`. La ruta debe ser pública (`permitAll()`). Añade validación
`@NotBlank` y `@Size(min = 8)` a la contraseña.

## Solución

> **Nota:** el enunciado pide guardar el rol como `"ROLE_USER"`, pero
> `BibliotecaUserDetailsService` (sección 13.4) usa `User.builder().roles(credencial.getRol())`,
> y `UserDetails.RoleBuilder.roles(...)` **añade** el prefijo `ROLE_` internamente —
> exactamente por eso `SeguridadConfig` declara sus reglas como `hasRole("ADMIN")` (sin
> prefijo) en vez de `hasAuthority("ROLE_ADMIN")`, y `data.sql` guarda `'ADMIN'`/`'USER'`
> sin prefijo. Guardar `"ROLE_USER"` literalmente en la columna `rol` produciría un rol
> efectivo `ROLE_ROLE_USER` (doble prefijo) que no coincidiría con ninguna regla de
> `SecurityFilterChain`. Esta solución guarda `"USER"`, coherente con el resto del proyecto.

DTO de entrada, con la validación pedida:

```java
package com.javaesencial.biblioteca.controlador;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroCredencialPeticion(
        @NotBlank(message = "El usuario es obligatorio")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password) {
}
```

`AuthController` nuevo:

```java
package com.javaesencial.biblioteca.controlador;

import com.javaesencial.biblioteca.dominio.Credencial;
import com.javaesencial.biblioteca.repositorio.CredencialJpaRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación", description = "Registro de credenciales de acceso a la API")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final CredencialJpaRepository credenciales;
    private final PasswordEncoder passwordEncoder;

    public AuthController(CredencialJpaRepository credenciales, PasswordEncoder passwordEncoder) {
        this.credenciales = credenciales;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public void registrar(@Valid @RequestBody RegistroCredencialPeticion peticion) {
        Credencial credencial = new Credencial(
                null,
                peticion.username(),
                passwordEncoder.encode(peticion.password()),
                "USER");
        credenciales.save(credencial);
    }
}
```

`SeguridadConfig` con la ruta nueva marcada como pública:

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.GET, "/libros", "/libros/**").permitAll()
        .requestMatchers("/docs/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
        .requestMatchers(HttpMethod.POST, "/auth/registro").permitAll()
        .requestMatchers(HttpMethod.POST, "/libros").hasRole("ADMIN")
        .requestMatchers(HttpMethod.PUT, "/libros/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/libros/**").hasRole("ADMIN")
        .requestMatchers("/prestamos/**").hasAnyRole("ADMIN", "USER")
        .anyRequest().authenticated())
```

`PasswordEncoder` no hay que declararlo de nuevo: `SeguridadConfig` ya expone el bean
`BCryptPasswordEncoder` (sección 13.4), y `AuthController` lo recibe por inyección de
constructor como cualquier otro bean de Spring.

### Por qué no se devuelve la `Credencial` guardada

El endpoint responde `201 Created` sin cuerpo (`void`, con `@ResponseStatus`), a
diferencia de `POST /libros` (que sí devuelve el `Libro` creado). Devolver la
`Credencial` completa expondría el hash BCrypt de la contraseña en la respuesta —aunque
esté hasheado, no hay ninguna razón para que el cliente lo reciba de vuelta—; devolver un
DTO sin el `password` sería una alternativa razonable, pero esta solución opta por lo más
simple que no filtra nada sensible.

### Comprobación

```terminal
POST /auth/registro
Content-Type: application/json

{ "username": "nuevo_lector", "password": "clave1234" }
```

```terminal
HTTP/1.1 201
```

Sin credenciales (ruta pública, gracias al `permitAll()` añadido). Con una contraseña de
menos de 8 caracteres:

```terminal
POST /auth/registro
Content-Type: application/json

{ "username": "nuevo_lector", "password": "corta" }
```

```terminal
HTTP/1.1 400
```
```json
{
  "type": "about:blank",
  "title": "Datos de entrada no válidos",
  "status": 400,
  "detail": "password: La contraseña debe tener al menos 8 caracteres"
}
```

(Vía `ManejadorGlobalErrores.manejarValidacion`, el mismo manejador de
`MethodArgumentNotValidException` que ya usan `Libro` y `Usuario`.)
