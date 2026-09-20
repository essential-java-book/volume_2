# Práctica 11.2 — Paginación en los préstamos activos

## Enunciado

Modifica `GET /prestamos` para que soporte paginación con los mismos parámetros que
`GET /libros` (`pagina`, `tamanio`). El repositorio debe devolver `Page<Prestamo>` y el
controlador `Page<PrestamoRespuesta>`. Usa `map()` sobre `Page` para transformar los
elementos manteniendo los metadatos de paginación.

## Solución

`PrestamoJpaRepository` con la consulta paginada añadida:

```java
public interface PrestamoJpaRepository extends JpaRepository<Prestamo, Long> {

    @Query("""
            SELECT p FROM Prestamo p JOIN FETCH p.usuario JOIN FETCH p.libro
            WHERE p.fechaDevolucion IS NULL
            """)
    List<Prestamo> buscarActivosConUsuarioYLibro();

    @Query(value = """
            SELECT p FROM Prestamo p JOIN FETCH p.usuario JOIN FETCH p.libro
            WHERE p.fechaDevolucion IS NULL
            """,
            countQuery = "SELECT COUNT(p) FROM Prestamo p WHERE p.fechaDevolucion IS NULL")
    Page<Prestamo> buscarActivosConUsuarioYLibroPaginado(Pageable pageable);

    // ... buscarActivosPorUsuarioConLibro y buscarPorIdConUsuarioYLibro, sin cambios ...
}
```

Se añade un `countQuery` explícito en vez de dejar que Spring Data derive uno
automáticamente: el conteo no necesita los `JOIN FETCH` de `usuario`/`libro` (solo cuenta
filas de `Prestamo`), así que escribirlo a mano evita que la consulta de conteo —que se
ejecuta en cada página, solo para saber `totalElements`— haga los mismos *joins* que la
consulta de datos sin ninguna necesidad.

`PrestamoService` con el método nuevo:

```java
@Transactional(readOnly = true)
public Page<Prestamo> obtenerTodosPaginado(Pageable pageable) {
    return prestamos.buscarActivosConUsuarioYLibroPaginado(pageable);
}
```

`PrestamoController` con `listarTodos` modificado:

```java
@GetMapping
public Page<PrestamoRespuesta> listarTodos(
        @RequestParam(defaultValue = "0") int pagina,
        @RequestParam(defaultValue = "10") int tamanio) {
    Pageable pageable = PageRequest.of(pagina, tamanio);
    return service.obtenerTodosPaginado(pageable).map(PrestamoRespuesta::desde);
}
```

### Por qué `Page<Prestamo>.map(...)` es la pieza clave

`Page<T>` no es solo una lista: además de los elementos, guarda `totalElements`,
`totalPages`, `number` (página actual) y `size`. Si esta práctica hubiera hecho
`.getContent().stream().map(PrestamoRespuesta::desde).toList()`, el resultado sería una
`List<PrestamoRespuesta>` que ha perdido todos esos metadatos — el cliente ya no sabría
cuántas páginas hay en total. `Page.map(Function)` en cambio conserva el mismo objeto
`Page` (misma página actual, mismo tamaño, mismo total), solo sustituye cada elemento por
el resultado de aplicarle la función — exactamente el mismo patrón que ya usa
`GET /libros` con `Page<Libro>` en el cuerpo del capítulo, aplicado aquí a la conversión
`Prestamo` → `PrestamoRespuesta`.

### Por qué el `JOIN FETCH` no impide paginar en la base de datos

Con relaciones `@OneToMany`/`@ManyToMany`, combinar `JOIN FETCH` con `Pageable` es
problemático: Hibernate no puede aplicar `LIMIT`/`OFFSET` en SQL sin arriesgarse a cortar
una colección a medias, así que termina trayendo *todas* las filas y paginando en memoria
(el célebre aviso `HHH000104`). Aquí no aplica: `Prestamo.usuario` y `Prestamo.libro` son
`@ManyToOne` (cada préstamo tiene exactamente un usuario y un libro, no una colección), así
que el `JOIN FETCH` no multiplica filas por prestamo y Hibernate puede paginar de verdad en
la base de datos, con `LIMIT`/`OFFSET` reales en el SQL generado — no hay ningún
compromiso de rendimiento distinto al de una consulta paginada sin `JOIN FETCH`.

### Comportamiento del endpoint tras el cambio

`GET /prestamos` (sin parámetros) deja de devolver automáticamente *todos* los préstamos
activos: ahora aplica los valores por defecto (`pagina=0`, `tamanio=10`), igual que ya hace
`GET /libros`. Un cliente que dependiera del comportamiento anterior (lista completa sin
paginar) tendría que pedir explícitamente un `tamanio` mayor que el número total de
préstamos activos para seguir viéndolos todos en una sola respuesta.
