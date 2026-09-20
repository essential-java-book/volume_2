# Práctica 5.3 — Búsqueda por título y autor

## Enunciado

Añade un endpoint `GET /libros/buscar` que acepte parámetros opcionales `titulo` y
`autor` y devuelva los libros que coincidan con ambos filtros simultáneamente:

```java
@GetMapping("/buscar")
public List<Libro> buscar(@RequestParam(required = false) String titulo,
                          @RequestParam(required = false) String autor) {
    // Filtra por título y/o autor según los parámetros recibidos
}
```

1. ¿Qué devuelve `GET /libros/buscar` sin ningún parámetro?
2. ¿Qué devuelve `GET /libros/buscar?autor=martin`?
3. ¿Qué devuelve `GET /libros/buscar?titulo=clean&autor=martin`?
4. ¿Dónde debe vivir la lógica de filtrado: en el controlador o en el servicio? ¿Por qué?

## Solución

> **Nota:** `@GetMapping("/buscar")` (sin `/libros` delante) es correcto aquí, a
> diferencia de lo que pasaba en el Capítulo 3: `LibroController` ya lleva
> `@RequestMapping("/libros")` en la clase desde este capítulo, así que
> `"/buscar"` se combina con ese prefijo y la ruta final es `/libros/buscar`.

`LibroController` con el endpoint de búsqueda añadido:

```java
@GetMapping("/buscar")
public List<Libro> buscar(@RequestParam(required = false) String titulo,
                           @RequestParam(required = false) String autor) {
    return service.buscar(titulo, autor);
}
```

Y en `BibliotecaService`:

```java
public List<Libro> buscar(String titulo, String autor) {
    return repositorio.findAll().stream()
            .filter(l -> titulo == null || titulo.isBlank()
                    || l.getTitulo().toLowerCase().contains(titulo.toLowerCase()))
            .filter(l -> autor == null || autor.isBlank()
                    || l.getAutor().toLowerCase().contains(autor.toLowerCase()))
            .toList();
}
```

### 1. `GET /libros/buscar` sin parámetros

Devuelve **todos los libros**, sin filtrar. Con `titulo` y `autor` a `null`, las dos
condiciones `titulo == null || ...` y `autor == null || ...` son ciertas para cualquier
libro (el primer término del `||` ya se cumple), así que ningún libro queda excluido por
ninguno de los dos filtros.

### 2. `GET /libros/buscar?autor=martin`

Devuelve los libros cuyo autor contenga "martin" (sin distinguir mayúsculas/minúsculas),
sin restricción de título. Con los tres libros de partida (El Quijote/Cervantes,
1984/Orwell, Dune/Herbert), ninguno coincide, así que la respuesta es `[]` — el mismo
resultado que ya se vio con la búsqueda equivalente del Capítulo 3, ahora con dos
parámetros posibles en vez de uno.

### 3. `GET /libros/buscar?titulo=clean&autor=martin`

Aplica **ambos filtros a la vez**: solo devolvería libros cuyo título contenga "clean" **y**
cuyo autor contenga "martin", simultáneamente. Con el catálogo de este volumen no hay
ningún libro así, así que también devuelve `[]`. (El enunciado usa deliberadamente
"clean"/"martin" como referencia a *Clean Code*, de Robert C. Martin — un libro que no
forma parte del catálogo de la Biblioteca de este volumen, así que el resultado vacío es
el esperado y no un error.)

### 4. Dónde debe vivir la lógica de filtrado

En el **servicio**, como ya se hizo. Las mismas razones que en la Práctica 3.3: el
controlador debe limitarse a traducir la petición HTTP (leer los `@RequestParam`,
delegar, devolver la respuesta) sin decidir por sí mismo las reglas de qué es "una
coincidencia"; esas reglas son lógica de negocio, reutilizable fuera del contexto HTTP
y más fácil de testear de forma aislada (con un test de servicio normal, sin `MockMvc`).
Aquí además hay un motivo añadido: combinar dos filtros independientes
(`titulo`/`autor`) con la misma lógica de "si no viene, no filtra por eso" es exactamente
el tipo de regla que conviene tener en un solo sitio, no duplicada si en el futuro otro
punto de la aplicación (no HTTP) necesitara la misma búsqueda combinada.
