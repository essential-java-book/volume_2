# Práctica 5.2 — Cabecera `Location` en la respuesta `201`

## Enunciado

La convención REST indica que la respuesta `201 Created` debe incluir una cabecera
`Location` con la URL del recurso recién creado. Modifica el método `crear` para
incluirla:

```java
@PostMapping
public ResponseEntity<Libro> crear(@RequestBody Libro libro,
                                   UriComponentsBuilder ucb) {
    Libro creado = service.registrar(libro);
    URI location = ucb.path("/libros/{id}")
                      .buildAndExpand(creado.getId())
                      .toUri();
    return ResponseEntity.created(location).body(creado);
}
```

1. Ejecuta `POST /libros` y observa las cabeceras de la respuesta en IntelliJ.
2. ¿Aparece la cabecera `Location`? ¿A qué URL apunta?
3. ¿Cuál es la diferencia entre `ResponseEntity.created(uri)` y
   `ResponseEntity.status(HttpStatus.CREATED)`?

## Solución

`LibroController.crear` con la cabecera añadida (recuerda que la clase ya lleva
`@RequestMapping("/libros")`, así que `ucb.path("/libros/{id}")` construye la URL
completa desde la raíz, no relativa al controlador):

```java
package com.javaesencial.biblioteca.controlador;

import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.servicio.BibliotecaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/libros")
public class LibroController {

    private final BibliotecaService service;

    public LibroController(BibliotecaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Libro> listarTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Libro> obtenerPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Libro> crear(@RequestBody Libro libro,
                                        UriComponentsBuilder ucb) {
        Libro creado = service.registrar(libro);
        URI location = ucb.path("/libros/{id}")
                .buildAndExpand(creado.getId())
                .toUri();
        return ResponseEntity.created(location).body(creado);
    }

    // obtenerPorId, actualizar y eliminar sin cambios
}
```

`UriComponentsBuilder` es un parámetro más del método: Spring lo inyecta automáticamente
en cada petición (no hace falta declararlo como bean ni pedirlo por constructor),
preconfigurado con el host y el puerto reales desde los que llegó la petición.

### 1 y 2. `POST /libros` y la cabecera `Location`

Con el mismo cuerpo de ejemplo del `pruebas.http` del capítulo (`Fahrenheit 451`, Ray
Bradbury, 1953), y siendo este el cuarto libro que se registra (id 4, tras los tres de
`CargadorDatosPrueba`):

```terminal
HTTP/1.1 201
Location: http://localhost:8080/libros/4
Content-Type: application/json

{
  "id": 4,
  "titulo": "Fahrenheit 451",
  "autor": "Ray Bradbury",
  "anio": 1953
}
```

Sí, la cabecera `Location` aparece, y apunta a `http://localhost:8080/libros/4` — la URL
exacta desde la que se podría recuperar ese mismo libro con `GET /libros/4`. `ucb`
construye esa URL sustituyendo `{id}` por `creado.getId()` (el id que
`RepositorioLibros.agregar()` acaba de asignar con su `AtomicLong`), y usando como base el
host/puerto reales de la petición entrante — no un valor fijo escrito a mano, lo que
también hace que funcione igual en un servidor con otro nombre de host distinto en
producción.

### 3. `ResponseEntity.created(uri)` frente a `ResponseEntity.status(HttpStatus.CREATED)`

`ResponseEntity.status(HttpStatus.CREATED)` (la versión original de `crear`, antes de esta
práctica) solo fija el código de estado a 201; cualquier cabecera adicional —como
`Location`— habría que añadirla aparte, a mano, con `.header(...)`. `ResponseEntity.created(uri)`
es un atajo que hace las dos cosas a la vez: fija el código a 201 **y** añade
automáticamente la cabecera `Location` con la URI que se le pase, sin tener que escribir
`.header(HttpHeaders.LOCATION, uri.toString())` por separado. El resultado final —usando
uno u otro— puede ser idéntico si se añade la cabecera a mano con el segundo, pero
`created(uri)` expresa directamente la intención ("he creado un recurso, y está aquí") en
una sola llamada, con menos posibilidad de olvidar la cabecera.
