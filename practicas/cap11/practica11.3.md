# Práctica 11.3 — Validar los campos de ordenación con @RequestParam

## Enunciado

Añade a `GET /libros` una validación que devuelva 400 Bad Request si el parámetro `orden`
contiene un valor no permitido (solo `id`, `titulo`, `autor`, `anio`). Añade el manejador
correspondiente en `ManejadorGlobalErrores`.

## Solución

`LibroController.listarTodos` con la validación añadida:

```java
private static final Set<String> CAMPOS_ORDEN_PERMITIDOS = Set.of("id", "titulo", "autor", "anio");

@Operation(summary = "Lista el catálogo, paginado y con búsqueda opcional")
@GetMapping
public Page<Libro> listarTodos(
        @RequestParam(defaultValue = "0") int pagina,
        @RequestParam(defaultValue = "10") int tamanio,
        @RequestParam(defaultValue = "titulo") String orden,
        @RequestParam(defaultValue = "asc") String direccion,
        @RequestParam(required = false) String buscar) {

    if (!CAMPOS_ORDEN_PERMITIDOS.contains(orden)) {
        throw new IllegalArgumentException(
                "Campo de ordenación no válido: '" + orden + "'. Valores permitidos: "
                        + CAMPOS_ORDEN_PERMITIDOS);
    }

    Sort.Direction sentido = "desc".equalsIgnoreCase(direccion)
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;
    Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by(sentido, orden));
    return service.obtenerTodosPaginado(pageable, buscar);
}
```

`ManejadorGlobalErrores` con el manejador nuevo:

```java
@ExceptionHandler(IllegalArgumentException.class)
public ProblemDetail manejarArgumentoInvalido(IllegalArgumentException ex) {
    return problema(HttpStatus.BAD_REQUEST, "Parámetro no válido", ex.getMessage());
}
```

(Reutiliza el método privado `problema(estado, titulo, detalle)` que ya usan
`manejarLibroNoEncontrado`, `manejarUsuarioNoEncontrado`, `manejarPrestamoNoEncontrado` y
`manejarValidacion` en esa clase.)

### Por qué la validación se hace a mano y no con Bean Validation

`orden` y `direccion` son `@RequestParam`, no campos de un objeto `@RequestBody` — Bean
Validation (`@NotBlank`, `@Pattern`, etc., que ya se usa en `Libro` y en `Usuario`) anota
propiedades de una clase, no parámetros sueltos de una petición GET. Podría anotarse el
propio parámetro con `@Pattern(regexp = "id|titulo|autor|anio")`, pero eso exigiría además
anotar la clase `LibroController` con `@Validated` para que Spring dispare la validación en
parámetros de método (algo que este proyecto no usa en ningún otro sitio todavía); la
comprobación manual con `Set.contains(...)` consigue el mismo resultado sin introducir un
mecanismo nuevo, y dejar la lista de valores permitidos en una constante hace explícito,
de un vistazo, cuáles son los únicos campos por los que se puede ordenar el catálogo.

### Relación con la Práctica 6.2

Esta es la segunda vez que el libro usa `IllegalArgumentException` + un manejador
específico en `ManejadorGlobalErrores` para señalar "el valor de un parámetro está fuera
de lo permitido" (la primera fue la Práctica 6.2, con los argumentos de un método del
servicio). Si esa práctica ya se hizo, `ManejadorGlobalErrores` ya tendría un manejador de
`IllegalArgumentException` — en ese caso no hace falta añadir uno nuevo, solo la
validación en el controlador; se incluye aquí el manejador completo asumiendo que esta
práctica se resuelve de forma independiente.

### Comprobación

```terminal
GET /libros?orden=inexistente
```

```terminal
HTTP/1.1 400
```
```json
{
  "type": "about:blank",
  "title": "Parámetro no válido",
  "status": 400,
  "detail": "Campo de ordenación no válido: 'inexistente'. Valores permitidos: [id, titulo, autor, anio]"
}
```
