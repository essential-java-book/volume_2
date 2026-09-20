# Práctica 10.1 — Endpoint para listar todos los usuarios

## Enunciado

Añade un `UsuarioController` con los endpoints `GET /usuarios` (listar todos) y
`GET /usuarios/{id}` (obtener uno por id). Documéntalos con `@Tag` y `@Operation`. Añade
también un endpoint `POST /usuarios` con validación `@Valid`.

## Solución

> **Nota:** el proyecto real de este volumen no incluye un `UsuarioController` propio —
> `Usuario`/`UsuarioJpaRepository` existen porque `Prestamo` los necesita, pero se
> gestionan solo internamente, a través de `PrestamoService`. Esta práctica lo añade
> como ejercicio, usando directamente `UsuarioJpaRepository` (sin una capa de servicio
> intermedia): no hay ninguna regla de negocio propia de "usuarios" en este capítulo más
> allá de un CRUD sencillo, así que un servicio adicional no aportaría nada que el
> controlador no pueda hacer llamando al repositorio directamente — a diferencia de
> `LibroController`/`PrestamoController`, que sí delegan siempre en un servicio.

```java
package com.javaesencial.biblioteca.controlador;

import com.javaesencial.biblioteca.dominio.Usuario;
import com.javaesencial.biblioteca.dominio.UsuarioNoEncontradoException;
import com.javaesencial.biblioteca.repositorio.UsuarioJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuarios", description = "Socios de la Biblioteca Municipal")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioJpaRepository repositorio;

    public UsuarioController(UsuarioJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Operation(summary = "Lista todos los usuarios registrados")
    @GetMapping
    public List<Usuario> listarTodos() {
        return repositorio.findAll();
    }

    @Operation(summary = "Obtiene un usuario por id")
    @GetMapping("/{id}")
    public Usuario obtenerPorId(@PathVariable Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    @Operation(summary = "Registra un usuario nuevo")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario crear(@Valid @RequestBody Usuario usuario) {
        return repositorio.save(usuario);
    }
}
```

`UsuarioNoEncontradoException` ya existe (la usa `PrestamoService` desde este mismo
capítulo) y `ManejadorGlobalErrores` ya la traduce a un 404 — no hace falta añadir nada
en esa clase para que `obtenerPorId` responda correctamente cuando el id no existe.

### Comprobación de la validación en `POST /usuarios`

`Usuario` ya lleva `@NotBlank` en `nombre` y `@NotBlank`/`@Email` en `email` (desde el
cuerpo del capítulo). Con `@Valid` en el parámetro, un `POST` con un email mal formado:

```json
{ "nombre": "Ana García", "email": "no-es-un-email" }
```

responde **400**, con el mismo `ManejadorGlobalErrores.manejarValidacion` que ya usa
`LibroController` desde el Capítulo 6 — `@ExceptionHandler(MethodArgumentNotValidException.class)`
es global (está en una clase `@RestControllerAdvice`, no ligada a un controlador
concreto), así que cualquier controlador nuevo que use `@Valid` queda cubierto
automáticamente, sin tener que repetir ni una línea de manejo de errores.
