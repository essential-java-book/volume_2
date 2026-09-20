# Cheat sheet · Capítulo 11 — Consultas avanzadas, paginación y ordenación

*Java Esencial · Volumen 2: Spring Boot · código del capítulo: tag `v2-cap11`*

## En una frase

Sabes escribir consultas con `@Query` (JPQL o SQL nativo) cuando Query Derivation se queda corto, y devolver el catálogo en páginas ordenadas con `Pageable`, `Page<T>` y `Sort` desde `GET /libros`.

## Lo esencial

| Elemento | Para qué sirve |
|---|---|
| JPQL | Lenguaje de consulta de JPA: opera sobre clases y campos Java (`FROM Libro l`), no sobre tablas y columnas |
| `@Query` | Escribe JPQL o SQL nativo en un método del repositorio |
| `@Param("texto")` | Vincula un parámetro del método con el marcador `:texto` de la consulta |
| `nativeQuery = true` | El valor de `@Query` es SQL puro del motor; no es portable entre bases de datos |
| `:param IS NULL OR campo = :param` | Filtro opcional: si el parámetro es `null`, la condición se ignora |
| `@Modifying` (con `@Transactional`) | Obligatorio junto a `@Query` en un UPDATE o DELETE; el método devuelve las filas afectadas |
| `Pageable` | Último parámetro de un método del repositorio: activa paginación y ordenación |
| `PageRequest.of(pagina, tamanio, sort)` | Implementación concreta de `Pageable`; las páginas empiezan en 0 |
| `Page<T>` | Datos más metadatos (`content`, `totalElements`, `totalPages`, `first`, `last`); ejecuta dos consultas: datos y COUNT |
| `Slice<T>` | Versión ligera sin COUNT: solo sabe si hay más (`hasNext()`); para scroll infinito o «cargar más» |
| `Sort.by("titulo").ascending()` | Ordenación dinámica; combina varios campos con `Sort.Order.asc(...)` y `Sort.Order.desc(...)` |
| `@RequestParam(defaultValue = "0")` | Lee parámetros de la URL (`?pagina=0&tamanio=10`); con `required = false` el parámetro es opcional |
| Proyección de interfaz | Interfaz con getters (`ResumenAnual`) que Spring Data implementa para recuperar solo unas columnas |
| `PageImpl` | Construye un `Page` a mano en los tests de `LibroControllerTest` |

## Código mínimo

Búsqueda por texto paginada en `LibroJpaRepository` (`findAll(Pageable)` ya viene con `JpaRepository`):

```java
@Query("""
        SELECT l FROM Libro l
        WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :texto, '%'))
           OR LOWER(l.autor) LIKE LOWER(CONCAT('%', :texto, '%'))
        """)
Page<Libro> buscarPorTextoPaginado(String texto, Pageable pageable);
```

El `listarTodos()` de `LibroController`, que ahora devuelve `Page<Libro>`:

```java
@GetMapping
public Page<Libro> listarTodos(
        @RequestParam(defaultValue = "0") int pagina,
        @RequestParam(defaultValue = "10") int tamanio,
        @RequestParam(defaultValue = "titulo") String orden,
        @RequestParam(defaultValue = "asc") String direccion,
        @RequestParam(required = false) String buscar) {

    Sort.Direction sentido = "desc".equalsIgnoreCase(direccion)
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;
    Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by(sentido, orden));
    return service.obtenerTodosPaginado(pageable, buscar);
}
```

`BibliotecaService` decide entre listar todo o buscar:

```java
public Page<Libro> obtenerTodosPaginado(Pageable pageable, String buscar) {
    if (buscar == null || buscar.isBlank()) {
        return repositorio.findAll(pageable);
    }
    return repositorio.buscarPorTextoPaginado(buscar, pageable);
}
```

## Comandos y peticiones

```http
### Página 0, 10 libros, ordenados por título (valores por defecto)
GET http://localhost:8080/libros

### Segunda página, 2 resultados por página
GET http://localhost:8080/libros?pagina=1&tamanio=2

### Ordenar por año descendente
GET http://localhost:8080/libros?orden=anio&direccion=desc

### Buscar en título o autor, con paginación y orden
GET http://localhost:8080/libros?buscar=code&pagina=0&tamanio=5&orden=anio&direccion=asc
```

## Errores típicos

- **`No property 'campoQueNoExiste' found for type 'Libro'`** → el cliente ha mandado un `orden` arbitrario. Valídalo contra un `Set.of("id", "titulo", "autor", "anio")` y usa `"titulo"` si no está.
- **`?tamanio=100000` tumba el servidor por memoria** → limita el tamaño con `Math.min(tamanio, TAMANIO_MAXIMO_PAGINA)` antes de construir el `Pageable`.
- **La búsqueda con `:texto` a `null` falla o no devuelve nada** → `CONCAT('%', NULL, '%')` da `NULL`. Comprueba `buscar != null && !buscar.isBlank()` antes de llamar al repositorio.
- **`?pagina=1` devuelve los libros del 11 al 20** → las páginas son base 0. Documéntalo con `@Parameter(description = "Número de página (base 0)", example = "0")` o resta 1 con `PageRequest.of(Math.max(0, pagina - 1), tamanio, sort)`.
- **`SemanticException: entity name used in query [libro] is not defined`** → has escrito SQL en una `@Query` JPQL. Usa el nombre de la clase y un alias: `SELECT l FROM Libro l`, nunca `SELECT * FROM libro`.

## En el Proyecto Biblioteca

`LibroJpaRepository` gana los métodos paginados (`findByTituloIgnoreCase`, `findByAutorIgnoreCase` y `buscarPorTextoPaginado` con `@Query`). `BibliotecaService` añade `obtenerTodosPaginado` y `GET /libros` pasa de devolver `List<Libro>` a `Page<Libro>`, con los parámetros `pagina`, `tamanio`, `orden`, `direccion` y `buscar`. En `LibroControllerTest`, el test del listado se reescribe con `PageImpl` y comprueba `$.content`.
