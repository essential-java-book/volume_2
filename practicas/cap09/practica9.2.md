# Práctica 9.2 — Endpoint GET /libros/autor/{autor}

## Enunciado

Usando el método de la práctica anterior, añade un nuevo endpoint
`GET /libros/autor/{autor}` en `LibroController` que devuelva la lista de libros del
autor indicado. Documéntalo con `@Operation` y `@Parameter`. Pruébalo con `pruebas.http`.

## Solución

```java
@Operation(summary = "Busca libros por autor (coincidencia exacta, sin distinguir mayúsculas)")
@GetMapping("/autor/{autor}")
public List<Libro> buscarPorAutor(
        @Parameter(description = "Nombre del autor, tal como aparece en el catálogo")
        @PathVariable String autor) {
    return service.buscarPorAutor(autor);
}
```

(Junto a los demás métodos de `LibroController`, que siguen con `@RequestMapping("/libros")`
a nivel de clase — la ruta completa queda `GET /libros/autor/{autor}`.)

### Prueba con `pruebas.http`

```http
### Capítulo 9 -- búsqueda por autor
GET http://localhost:8080/libros/autor/miguel de cervantes
```

```json
[
  { "id": 1, "titulo": "El Quijote", "autor": "Miguel de Cervantes", "anio": 1605 }
]
```

Aunque la URL lleva "miguel de cervantes" en minúsculas y el dato real en `data.sql` es
"Miguel de Cervantes", el resultado incluye el libro igualmente — `findByAutorIgnoreCase`
ignora la diferencia de mayúsculas/minúsculas, tal como pedía la Práctica 9.1. Con un
autor que no exista en el catálogo:

```http
GET http://localhost:8080/libros/autor/nadie
```

```json
[]
```

Devuelve una lista vacía, no un 404: a diferencia de `GET /libros/{id}`, que busca un
recurso único por su identificador y sí puede "no encontrarlo", esta búsqueda por autor
es una consulta que siempre tiene una respuesta válida —la lista de coincidencias, que
puede estar vacía—, igual que ya ocurría con la búsqueda por autor del Capítulo 3 (Práctica
3.3) y la búsqueda combinada del Capítulo 5 (Práctica 5.3): una lista vacía no es un
error, es un resultado legítimo de "no hay coincidencias".

### Coincidencia con `{autor}` en la ruta

Nótese que la ruta usa `@PathVariable` (`/autor/{autor}`, un segmento fijo de la URL),
no `@RequestParam` como las búsquedas de los Capítulos 3 y 5
(`/buscar?autor=...`). Ambos diseños son válidos en REST; aquí la elección la marca el
propio enunciado (`GET /libros/autor/{autor}`, con el autor como parte de la ruta), que
tiene sentido cuando se busca por un único criterio obligatorio —no opcional, como sí lo
eran `titulo`/`autor` en la Práctica 5.3, donde `@RequestParam(required = false)` permitía
omitirlos—.
