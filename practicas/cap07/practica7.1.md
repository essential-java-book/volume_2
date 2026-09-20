# Práctica 7.1 — Documentar el ManejadorGlobalErrores

## Enunciado

Actualmente `ManejadorGlobalErrores` no está documentado en la especificación OpenAPI.
Añade a cada método de `LibroController` la respuesta `400` con una descripción clara
cuando el cuerpo de la petición contiene datos inválidos, usando `@ApiResponse`.
Comprueba que Swagger UI muestra la nueva respuesta al expandir `POST /libros` y
`PUT /libros/{id}`.

## Solución

`crear` y `actualizar` son los dos únicos métodos de `LibroController` que reciben
`@Valid @RequestBody Libro libro` y, por tanto, los dos únicos que pueden disparar
`manejarValidacion` (400). `obtenerPorId` ya documenta su propio 404 desde el cuerpo del
capítulo; a `crear` y `actualizar` les falta el 400 correspondiente:

```java
package com.javaesencial.biblioteca.controlador;

import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.servicio.BibliotecaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Libros", description = "Catálogo de la Biblioteca")
@RestController
@RequestMapping("/libros")
public class LibroController {

    private final BibliotecaService service;

    public LibroController(BibliotecaService service) {
        this.service = service;
    }

    @Operation(summary = "Lista todos los libros del catálogo")
    @GetMapping
    public List<Libro> listarTodos() {
        return service.obtenerTodos();
    }

    @Operation(summary = "Obtiene un libro por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe ningún libro con ese id")
    })
    @GetMapping("/{id}")
    public Libro obtenerPorId(@Parameter(description = "Id del libro") @PathVariable Long id) {
        return service.buscarPorIdOFallar(id);
    }

    @Operation(summary = "Registra un libro nuevo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Libro creado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada no válidos (título/autor vacíos, año fuera de rango, etc.)")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Libro crear(@Valid @RequestBody Libro libro) {
        return service.registrar(libro);
    }

    @Operation(summary = "Actualiza un libro existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada no válidos (título/autor vacíos, año fuera de rango, etc.)"),
            @ApiResponse(responseCode = "404", description = "No existe ningún libro con ese id")
    })
    @PutMapping("/{id}")
    public Libro actualizar(@PathVariable Long id, @Valid @RequestBody Libro libro) {
        return service.actualizar(id, libro);
    }

    @Operation(summary = "Elimina un libro")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
```

(En `actualizar` se añade también el 404, que la operación puede devolver igual que
`obtenerPorId` — `BibliotecaService.actualizar` lanza `LibroNoEncontradoException` si el
id no existe — y que tampoco estaba documentado.)

### Comprobación en Swagger UI

Al abrir `/docs` y expandir `POST /libros`, la sección "Responses" pasa de mostrar solo
`201` a mostrar dos entradas: `201 Libro creado` y `400 Datos de entrada no válidos
(título/autor vacíos, año fuera de rango, etc.)`. Al expandir `PUT /libros/{id}`, se ven
las tres: `200`, `400` y `404`, cada una con su descripción — antes de la práctica, esa
operación no mostraba ninguna respuesta documentada explícitamente (solo la inferida por
defecto del tipo de retorno). El propio `@ApiResponses` no cambia el comportamiento real
de la API en ningún sentido —los códigos 400/404 ya los devolvía `ManejadorGlobalErrores`
desde el Capítulo 6—; solo hace visible en la documentación algo que ya ocurría, para que
quien consuma la API sepa, sin tener que leer el código fuente, qué respuestas de error
puede recibir.
