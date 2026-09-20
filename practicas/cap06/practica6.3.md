# Práctica 6.3 — Devolver los errores ordenados alfabéticamente

## Enunciado

Actualmente `manejarValidacion` concatena los errores de validación en el campo `detail`
en el orden en que Spring los entrega, que no está garantizado. Modifica el método para
que los errores aparezcan siempre **ordenados alfabéticamente por nombre de campo** antes
de unirlos con `Collectors.joining("; ")`.

Pista: ordena la lista de `FieldError` con
`.sorted(Comparator.comparing(FieldError::getField))` antes de mapear y unir.

## Solución

`manejarValidacion` tal como queda al terminar el cuerpo del capítulo (sin orden
garantizado — depende del orden interno de `getFieldErrors()`):

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ProblemDetail manejarValidacion(MethodArgumentNotValidException ex) {
    String detalle = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
    ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalle);
    problema.setTitle("Datos de entrada no válidos");
    return problema;
}
```

Con el `.sorted(...)` añadido, justo antes del `.map(...)`:

```java
import java.util.Comparator;
import org.springframework.validation.FieldError;

@ExceptionHandler(MethodArgumentNotValidException.class)
public ProblemDetail manejarValidacion(MethodArgumentNotValidException ex) {
    String detalle = ex.getBindingResult().getFieldErrors().stream()
            .sorted(Comparator.comparing(FieldError::getField))
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
    ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalle);
    problema.setTitle("Datos de entrada no válidos");
    return problema;
}
```

### Comprobación

Con un `POST /libros` que viole varias reglas a la vez —por ejemplo, sin `titulo`, con
`autor` vacío y con `anio` fuera de rango—:

```json
{
  "titulo": "",
  "autor": "",
  "anio": 3000
}
```

Antes del cambio, el orden de `titulo`/`autor`/`anio` en `detail` podía variar de una
ejecución a otra (depende del orden interno con el que Bean Validation recorre las
restricciones, que no está especificado ni garantizado por el estándar). Con
`.sorted(Comparator.comparing(FieldError::getField))`, el resultado es siempre el mismo,
ordenado alfabéticamente por nombre de campo (`anio` < `autor` < `titulo`):

```json
{
  "type": "about:blank",
  "title": "Datos de entrada no válidos",
  "status": 400,
  "detail": "anio: El año no puede ser posterior a 2100; autor: El autor es obligatorio; titulo: El título es obligatorio"
}
```

`Comparator.comparing(FieldError::getField)` construye un comparador que ordena los
`FieldError` por el resultado de su método `getField()` (el nombre del campo que falló,
`"anio"`, `"autor"`, `"titulo"`), usando el orden natural de `String` — es decir,
alfabético. Que la respuesta sea siempre igual para la misma petición no es solo una
cuestión estética: hace que los tests que comprueben el contenido exacto de `detail` (como
los que se escribirán con `MockMvc` en el Capítulo 8) sean deterministas, en vez de fallar
de forma intermitente según el orden en que Bean Validation decida entregar los errores
esa vez.
