# Práctica 11.1 — Búsqueda por rango de años

## Enunciado

Añade en `LibroJpaRepository` el método con `@Query` que devuelva los libros publicados
entre dos años (inclusive), ordenados por año ascendente. La firma debe ser:

```java
@Query("SELECT l FROM Libro l WHERE l.anio BETWEEN :desde AND :hasta ORDER BY l.anio ASC")
List<Libro> findByAnioBetween(@Param("desde") int desde, @Param("hasta") int hasta);
```

Añade el endpoint `GET /libros/rango?desde=1990&hasta=2010` en `LibroController`.

## Solución

`LibroJpaRepository` con el método añadido (firma exacta del enunciado):

```java
public interface LibroJpaRepository extends JpaRepository<Libro, Long> {

    Page<Libro> findByTituloIgnoreCase(String titulo, Pageable pageable);

    Page<Libro> findByAutorIgnoreCase(String autor, Pageable pageable);

    @Query("""
            SELECT l FROM Libro l
            WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(l.autor) LIKE LOWER(CONCAT('%', :texto, '%'))
            """)
    Page<Libro> buscarPorTextoPaginado(String texto, Pageable pageable);

    @Query("SELECT l FROM Libro l WHERE l.anio BETWEEN :desde AND :hasta ORDER BY l.anio ASC")
    List<Libro> findByAnioBetween(@Param("desde") int desde, @Param("hasta") int hasta);
}
```

`BibliotecaService` con el método nuevo (el controlador de este proyecto nunca llama al
repositorio directamente — siempre delega en el servicio, como hacen ya `obtenerTodos`,
`buscarPorIdOFallar`, etc.):

```java
public List<Libro> obtenerPorRangoDeAnios(int desde, int hasta) {
    return repositorio.findByAnioBetween(desde, hasta);
}
```

`LibroController` con el endpoint nuevo:

```java
@Operation(summary = "Lista los libros publicados entre dos años, ambos inclusive")
@GetMapping("/rango")
public List<Libro> porRangoDeAnios(
        @RequestParam int desde,
        @RequestParam int hasta) {
    return service.obtenerPorRangoDeAnios(desde, hasta);
}
```

### Por qué esta consulta no devuelve `Page<Libro>`

A diferencia de `findByTituloIgnoreCase`/`findByAutorIgnoreCase`/`buscarPorTextoPaginado`
(las tres consultas paginadas del cuerpo del capítulo, pensadas para listar un catálogo
potencialmente grande), el enunciado fija explícitamente la firma de
`findByAnioBetween` devolviendo `List<Libro>`, no `Page<Libro>` — coherente con que es una
consulta acotada por naturaleza (un rango de años suele devolver un conjunto pequeño y
concreto de resultados, no todo el catálogo), y por eso el endpoint tampoco expone
`pagina`/`tamanio`: simplemente devuelve la lista completa que cae dentro del rango,
ordenada por año.

### Comprobación

```terminal
GET /libros/rango?desde=1990&hasta=2010
```

```json
[
  { "id": 7, "titulo": "La sombra del viento", "autor": "Carlos Ruiz Zafón", "anio": 2001 },
  { "id": 9, "titulo": "2666", "autor": "Roberto Bolaño", "anio": 2004 }
]
```

(Los datos exactos dependen de `data.sql`; lo que importa es la forma de la respuesta —
solo libros con `anio` entre 1990 y 2010, ambos inclusive, ordenados de forma ascendente
por año, tal como fijan el `BETWEEN` y el `ORDER BY l.anio ASC` de la consulta.)
