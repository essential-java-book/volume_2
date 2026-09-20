# Práctica 11.4 — Proyección de solo título y autor

## Enunciado

Crea la interfaz de proyección `LibroResumen`:

```java
public interface LibroResumen {
    Long getId();
    String getTitulo();
    String getAutor();
}
```

Añade en el repositorio el método:

```java
List<LibroResumen> findAllProjectedBy();
```

Y el endpoint `GET /libros/resumen` que devuelva la lista de proyecciones. Verifica que el
JSON solo contiene `id`, `titulo` y `autor`, sin el campo `anio`.

## Solución

`LibroResumen`, en el paquete `dominio` (junto a `Libro`):

```java
package com.javaesencial.biblioteca.dominio;

/**
 * Proyección de solo lectura sobre {@code Libro}: expone únicamente
 * id, título y autor, sin el año. Spring Data JPA genera la
 * implementación en tiempo de ejecución (un proxy) a partir de los
 * nombres de estos métodos — no hace falta escribir ninguna clase
 * que la implemente.
 */
public interface LibroResumen {
    Long getId();
    String getTitulo();
    String getAutor();
}
```

`LibroJpaRepository` con el método añadido:

```java
public interface LibroJpaRepository extends JpaRepository<Libro, Long> {

    // ... los métodos del cuerpo del capítulo y de la Práctica 11.1, sin cambios ...

    List<LibroResumen> findAllProjectedBy();
}
```

`BibliotecaService` con el método nuevo:

```java
public List<LibroResumen> obtenerResumen() {
    return repositorio.findAllProjectedBy();
}
```

`LibroController` con el endpoint nuevo:

```java
@Operation(summary = "Lista el catálogo con solo id, título y autor")
@GetMapping("/resumen")
public List<LibroResumen> resumen() {
    return service.obtenerResumen();
}
```

### Cómo resuelve Spring Data `findAllProjectedBy()`

`findAllProjectedBy()` no es un método derivado normal (no hay ningún campo llamado
`Projected`): `ProjectedBy` es una palabra clave especial que Spring Data reconoce como "el
equivalente a `findAll()`, pero devolviendo el tipo de proyección declarado en el genérico
del método" — aquí, `LibroResumen`. En tiempo de arranque, Spring Data genera un *proxy*
dinámico de la interfaz `LibroResumen` por cada `Libro` recuperado, y ese proxy delega cada
getter (`getId()`, `getTitulo()`, `getAutor()`) en la columna correspondiente — Hibernate,
además, es capaz de generar el SQL para que la consulta **solo pida esas tres columnas** a
la base de datos (ni siquiera trae `anio`), a diferencia de traer el `Libro` completo y
descartar el campo después en Java.

### Comprobación

```terminal
GET /libros/resumen
```

```json
[
  { "id": 1, "titulo": "Cien años de soledad", "autor": "Gabriel García Márquez" },
  { "id": 2, "titulo": "1984", "autor": "George Orwell" }
]
```

Ningún elemento del array lleva la clave `anio` — no es que el campo llegue con valor
`null` y se omita por configuración de Jackson: `LibroResumen` sencillamente no declara
`getAnio()`, así que no hay ninguna propiedad de la que Jackson pueda generar esa clave al
serializar el proxy.

### Por qué es distinto de un DTO como `PrestamoRespuesta`

`PrestamoRespuesta` (Capítulo 10) es un `record` normal: Java lo compila con todos sus
campos y su cuerpo entero, y `PrestamoRespuesta.desde(prestamo)` construye la instancia a
mano copiando valores ya cargados en memoria. `LibroResumen`, en cambio, es solo una
interfaz —sin ninguna clase que la implemente en el código fuente—, y la proyección ocurre
un nivel más abajo: en la propia consulta SQL que genera Spring Data, no en una conversión
posterior en Java. El resultado observable es parecido (un JSON más pequeño que la entidad
completa), pero una proyección de interfaz como esta ahorra trabajo también en la base de
datos, mientras que un DTO como `PrestamoRespuesta` sigue trayendo la entidad completa y
recorta campos solo al construir la respuesta.
