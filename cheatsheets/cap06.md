# Cheat sheet · Capítulo 6 — Validación y manejo global de errores

*Java Esencial · Volumen 2: Spring Boot · código del capítulo: tag `v2-cap06`*

## En una frase

Rechazas los datos inválidos en la capa de entrada con Bean Validation y `@Valid`, y centralizas los errores en un `@RestControllerAdvice` que responde con `ProblemDetail` (RFC 9457): 400 con el detalle de cada campo y 404 cuando el libro no existe.

## Lo esencial

| Elemento | Para qué sirve |
|---|---|
| `spring-boot-starter-validation` | Starter que trae Hibernate Validator; no viene incluido en `spring-boot-starter-web` |
| `@NotBlank` | El `String` no puede ser `null`, vacío ni solo espacios (`@NotEmpty` sí admite espacios) |
| `@NotNull` | El campo no puede ser `null`; funciona con cualquier tipo |
| `@Min` / `@Max` | Valor mínimo y máximo (inclusivos) para campos numéricos |
| `@Size(min, max)` | Longitud mínima y máxima para `String` y colecciones |
| `Integer` en vez de `int` | Con `int`, Jackson pone 0 si el campo no llega; con `Integer` queda `null` y `@NotNull` lo detecta |
| `@Valid` | Delante del parámetro `@RequestBody`: activa la validación antes de ejecutar el método |
| `MethodArgumentNotValidException` | La lanza Spring cuando falla la validación de un `@RequestBody` |
| `@RestControllerAdvice` | Clase que maneja las excepciones de todos los controladores |
| `@ExceptionHandler(X.class)` | Método que maneja un tipo de excepción concreto |
| `ProblemDetail` | Formato de error RFC 9457: `type`, `title`, `status`, `detail`, `instance`; extras con `setProperty(clave, valor)` |
| `LibroNoEncontradoException` | Excepción de dominio propia (`RuntimeException`) que el manejador traduce a 404 |
| `@ResponseStatus(HttpStatus.CREATED)` | Fija el código de estado cuando el método ya no devuelve `ResponseEntity` |
| Errores 4xx / 5xx | 4xx: el fallo es del cliente (400, 404, 409); 5xx: el fallo es del servidor (500, 503) |

## Código mínimo

Las restricciones en `dominio/Libro.java` (`autor` se anota igual que `titulo`, con máximo 150).

```java
@NotBlank(message = "El título es obligatorio")
@Size(min = 1, max = 200, message = "El título no puede superar los 200 caracteres")
private String titulo;

@NotNull(message = "El año es obligatorio")
@Min(value = 1450, message = "El año no puede ser anterior a 1450")
@Max(value = 2100, message = "El año no puede ser posterior a 2100")
private Integer anio;
```

El manejador global en `controlador/ManejadorGlobalErrores.java`.

```java
@RestControllerAdvice
public class ManejadorGlobalErrores {

    @ExceptionHandler(LibroNoEncontradoException.class)
    public ProblemDetail manejarLibroNoEncontrado(LibroNoEncontradoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setTitle("Libro no encontrado");
        return problema;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarValidacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalle);
        problema.setTitle("Datos de entrada no válidos");
        return problema;
    }
}
```

El controlador, ya sin `ResponseEntity` ni `Optional`: el servicio lanza la excepción y el manejador responde.

```java
@GetMapping("/{id}")
public Libro obtenerPorId(@PathVariable Long id) {
    return service.buscarPorIdOFallar(id);
}

@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public Libro crear(@Valid @RequestBody Libro libro) {
    return service.registrar(libro);
}

// BibliotecaService
public Libro buscarPorIdOFallar(Long id) {
    return buscarPorId(id).orElseThrow(() -> new LibroNoEncontradoException(id));
}
```

## Errores típicos

- **Un libro inválido se guarda con 201 y sin ningún error** → `@Valid` está sobre el método; va delante del parámetro: `crear(@Valid @RequestBody Libro libro)`.
- **`package jakarta.validation.constraints does not exist` al compilar** → falta `spring-boot-starter-validation` en el `pom.xml`.
- **`UnexpectedTypeException: No validator could be found for constraint 'NotBlank' validating type 'int'`** → `@NotBlank` solo vale para `String`; en números usa `@NotNull` (con `Integer`) junto con `@Min` y `@Max`.
- **El manejador nunca se ejecuta y llega el 400 genérico de Spring** → capturas `ValidationException`; la que lanza `@Valid` es `MethodArgumentNotValidException`.
- **`IllegalStateException: Ambiguous @ExceptionHandler method mapped` al arrancar** → dos `@RestControllerAdvice` manejan la misma excepción; reúne todos los `@ExceptionHandler` en una única clase.

## En el Proyecto Biblioteca

Entra `spring-boot-starter-validation` en el `pom.xml` y `Libro` se anota con restricciones (`anio` pasa de `int` a `Integer`). Nacen `dominio/LibroNoEncontradoException` y `controlador/ManejadorGlobalErrores`. `BibliotecaService` gana `buscarPorIdOFallar`, y `actualizar`/`eliminar` lanzan la excepción en vez de devolver `Optional`/`boolean`; `LibroController` usa `@Valid` y `@ResponseStatus` y deja de usar `ResponseEntity`. `pruebas.http` no cambia en este capítulo.
