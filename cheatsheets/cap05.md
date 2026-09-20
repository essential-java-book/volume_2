# Cheat sheet · Capítulo 5 — Controladores REST y CRUD completo

*Java Esencial · Volumen 2: Spring Boot · código del capítulo: tag `v2-cap05`*

## En una frase

Construyes los cinco endpoints de una API RESTful sobre `/libros` y devuelves en cada uno el código de estado correcto (200, 201, 204, 404) con `ResponseEntity`, `@PathVariable` y `@RequestBody`.

## Lo esencial

| Elemento | Para qué sirve |
|---|---|
| CRUD | Create, Read, Update, Delete: las cuatro operaciones básicas sobre un recurso |
| `GET` | Leer; nunca modifica el estado del servidor. Devuelve 200 (o 404 si el recurso no existe) |
| `POST` | Crear un recurso nuevo; devuelve 201 Created |
| `PUT` | Reemplazar un recurso completo; devuelve 200 OK o 404 |
| `DELETE` | Eliminar un recurso; devuelve 204 No Content o 404 |
| `ResponseEntity<T>` | Envoltorio de la respuesta HTTP: cuerpo, código de estado y cabeceras. Sin cuerpo, `<Void>` |
| `ResponseEntity.ok(body)` | 200 OK con cuerpo |
| `ResponseEntity.status(HttpStatus.CREATED).body(b)` | 201 Created con cuerpo |
| `ResponseEntity.noContent().build()` | 204 No Content sin cuerpo |
| `ResponseEntity.notFound().build()` | 404 Not Found sin cuerpo |
| `@PathVariable` | Extrae el segmento `{id}` de la URL y lo convierte al tipo declarado; si no puede (`/libros/abc`), Spring responde 400 |
| `@RequestBody` | Deserializa el cuerpo JSON al objeto Java (Jackson); necesita constructor vacío y getters/setters. JSON mal formado: 400 |
| `@PutMapping("/{id}")` / `@DeleteMapping("/{id}")` | Mapean peticiones PUT y DELETE a un método del controlador |
| Devolver `List<Libro>` directamente | Válido cuando el código siempre es 200; usa `ResponseEntity` solo si el código puede variar |

## Código mínimo

El patrón `Optional` → 200 o 404, y la creación con 201 (`LibroController`, con `@RequestMapping("/libros")`).

```java
@GetMapping("/{id}")
public ResponseEntity<Libro> obtenerPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
            .map(ResponseEntity::ok)                      // existe → 200 con el libro
            .orElse(ResponseEntity.notFound().build());   // no existe → 404 sin cuerpo
}

@PostMapping
public ResponseEntity<Libro> crear(@RequestBody Libro libro) {
    Libro creado = service.registrar(libro);
    return ResponseEntity.status(HttpStatus.CREATED).body(creado);
}
```

Eliminar responde 204 o 404. `PUT /libros/{id}` reutiliza el patrón del `GET /{id}` con `service.actualizar(id, libro)`.

```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> eliminar(@PathVariable Long id) {
    if (service.eliminar(id)) {
        return ResponseEntity.noContent().build();   // 204
    }
    return ResponseEntity.notFound().build();        // 404
}
```

## Comandos y peticiones

```http
### pruebas.http — CRUD completo de libros
GET http://localhost:8080/libros

###
POST http://localhost:8080/libros
Content-Type: application/json

{
  "titulo": "Fahrenheit 451",
  "autor": "Ray Bradbury",
  "anio": 1953
}

###
PUT http://localhost:8080/libros/1
Content-Type: application/json

{
  "titulo": "El Quijote",
  "autor": "Miguel de Cervantes",
  "anio": 1605
}

###
DELETE http://localhost:8080/libros/4
```

## Errores típicos

- **`POST /libros` responde 200 en lugar de 201** → el método devuelve `Libro` directamente; envuélvelo en `ResponseEntity.status(HttpStatus.CREATED).body(creado)`.
- **`DELETE` responde 200 con un texto en el cuerpo** → usa `ResponseEntity<Void>` con `noContent().build()` si se eliminó y `notFound().build()` si no existía.
- **`@PathVariable String id` y un `Long.parseLong(id)` a mano** → declara el parámetro como `Long`: Spring convierte el segmento y responde 400 si no es numérico.
- **El libro creado llega con todos los campos a `null` o `0`** → falta `@RequestBody` en el parámetro; sin él, Spring no lee el cuerpo JSON.
- **`GET /libros/buscar` da 400 al convertir `"buscar"` a `Long`** → la ruta literal la captura `/{id}`. En Spring Boot 3.x las rutas literales tienen siempre prioridad; en versiones anteriores, declara primero las literales o llévalas a otro controlador.

## En el Proyecto Biblioteca

`LibroController` pasa de un único `GET /libros` al CRUD completo: `GET /libros/{id}`, `POST /libros`, `PUT /libros/{id}` y `DELETE /libros/{id}`, todos con `ResponseEntity`. `RepositorioLibros` y `BibliotecaService` ganan `actualizar()` (el servicio delega en el repositorio y solo `registrar` escribe en el log, a nivel INFO). `pruebas.http` incorpora las peticiones de los cinco endpoints.
