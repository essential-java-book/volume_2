# Práctica 7.4 — Agrupar endpoints por tag

## Enunciado

Añade un segundo controlador ficticio, `EstadisticasController`, con un único endpoint
`GET /estadisticas/total` que devuelva el número de libros como un entero. Anótalo con
`@Tag(name = "Estadísticas")`. Comprueba que Swagger UI muestra dos grupos: "Libros" y
"Estadísticas".

## Solución

```java
package com.javaesencial.biblioteca.controlador;

import com.javaesencial.biblioteca.servicio.BibliotecaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Estadísticas", description = "Datos agregados del catálogo")
@RestController
@RequestMapping("/estadisticas")
public class EstadisticasController {

    private final BibliotecaService service;

    public EstadisticasController(BibliotecaService service) {
        this.service = service;
    }

    @Operation(summary = "Número total de libros del catálogo")
    @GetMapping("/total")
    public int total() {
        return service.obtenerTodos().size();
    }
}
```

`EstadisticasController` reutiliza `BibliotecaService.obtenerTodos()`, el mismo método
que ya usa `LibroController.listarTodos()` — no hace falta ningún servicio nuevo para un
cálculo tan simple como contar el tamaño de la lista.

### Comprobación

```terminal
GET http://localhost:8080/estadisticas/total
```
```terminal
3
```

(Con el perfil `dev` y los tres libros de partida sin modificar.)

En Swagger UI (`/docs`), la página pasa de mostrar un único grupo desplegable —"Libros",
con las cinco operaciones de `LibroController`— a mostrar **dos**: "Libros" (sin cambios)
y "Estadísticas", con la única operación `GET /estadisticas/total`. El agrupamiento en
Swagger UI se basa exactamente en el valor de `name` de `@Tag`: cada controlador anotado
con un `@Tag` distinto aparece como una sección separada y colapsable, con el `description`
del `@Tag` como subtítulo de esa sección — es la misma anotación `@Tag(name = "Libros",
...)` que ya llevaba `LibroController` desde el cuerpo del capítulo, aplicada ahora a un
segundo controlador para demostrar que el agrupamiento escala de forma natural a
cualquier número de controladores, cada uno con su propia área temática.
