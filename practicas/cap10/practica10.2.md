# Práctica 10.2 — Verificar que un libro no está ya prestado

## Enunciado

Modifica `PrestamoService.registrar` para que compruebe si el libro ya tiene un préstamo
activo antes de registrar uno nuevo. Si ya está prestado, lanzar
`IllegalStateException("El libro con id X ya está prestado")`. Añade un manejador para
`IllegalStateException` en `ManejadorGlobalErrores` que devuelva 409 Conflict.

## Solución

`PrestamoJpaRepository` necesita una consulta nueva para comprobar si un libro concreto
tiene ya un préstamo activo:

```java
public interface PrestamoJpaRepository extends JpaRepository<Prestamo, Long> {

    // ... las tres consultas @Query del cuerpo del capítulo, sin cambios ...

    boolean existsByLibroIdAndFechaDevolucionIsNull(Long libroId);
}
```

(Método derivado, no `@Query`: Spring Data lo construye a partir del nombre —
`existsBy` + `LibroId` [el id del libro relacionado, navegando la asociación
`libro.id`] + `And` + `FechaDevolucionIsNull` [la condición `fecha_devolucion IS NULL`
que ya usan las tres consultas manuales, aquí expresada por nombre en vez de JPQL]—.)

`PrestamoService.registrar` con la comprobación añadida:

```java
public Prestamo registrar(Long usuarioId, Long libroId) {
    Usuario usuario = usuarios.findById(usuarioId)
            .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));
    Libro libro = libros.findById(libroId)
            .orElseThrow(() -> new LibroNoEncontradoException(libroId));

    if (prestamos.existsByLibroIdAndFechaDevolucionIsNull(libroId)) {
        throw new IllegalStateException("El libro con id " + libroId + " ya está prestado");
    }

    Prestamo prestamo = new Prestamo(usuario, libro, LocalDate.now());
    return prestamos.save(prestamo);
}
```

La comprobación se hace **después** de confirmar que el usuario y el libro existen (igual
que antes), y **antes** de crear el nuevo `Prestamo`: no tiene sentido comprobar
disponibilidad de un libro que ni siquiera existe, y hay que comprobarla antes de guardar
nada, no después.

`ManejadorGlobalErrores` con el manejador nuevo:

```java
@ExceptionHandler(IllegalStateException.class)
public ProblemDetail manejarEstadoInvalido(IllegalStateException ex) {
    return problema(HttpStatus.CONFLICT, "Operación no permitida en el estado actual", ex.getMessage());
}
```

(Reutiliza el método privado `problema(estado, titulo, detalle)` que ya usan los otros
cuatro manejadores de la clase.)

### Comprobación

Con el libro `id=1` (El Quijote) ya prestado por un `POST
/prestamos/usuario/1/libro/1` previo, un segundo intento de prestar el mismo libro:

```terminal
POST /prestamos/usuario/2/libro/1
```

```terminal
HTTP/1.1 409
```
```json
{
  "type": "about:blank",
  "title": "Operación no permitida en el estado actual",
  "status": 409,
  "detail": "El libro con id 1 ya está prestado"
}
```

### Por qué 409 Conflict y no 400 Bad Request

**400** significa "la petición en sí está mal formada o contiene datos inválidos" —el
caso de `@Valid`/`MethodArgumentNotValidException`—. Aquí la petición es perfectamente
válida (un usuario y un libro que existen, en el formato correcto): el problema no está
en los datos que llegan, sino en que el **estado actual del sistema** (el libro ya
prestado) hace imposible completar la operación tal como se pide en este momento. Ese es
exactamente el significado de **409 Conflict** en el estándar HTTP: la petición entra en
conflicto con el estado actual del recurso. Es la misma distinción que ya separaba
`IllegalArgumentException` (Práctica 6.2, dato de entrada fuera de rango, 400) de esta
`IllegalStateException` (aquí, una regla que depende del estado de otro recurso, 409): el
tipo de excepción refleja la naturaleza real del problema, y el código de estado se
deriva de esa naturaleza, no al revés.
