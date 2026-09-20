# Práctica 6.4 — Incluir el `instance` en el ProblemDetail

## Enunciado

El campo `instance` del RFC 9457 debe contener la URI de la petición que causó el error.
Modifica `manejarValidacion` para que reciba también `HttpServletRequest request` y use
`request.getRequestURI()` para rellenar `problema.setInstance(...)`.

## Solución

```java
package com.javaesencial.biblioteca.controlador;

import com.javaesencial.biblioteca.dominio.LibroNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.springframework.validation.FieldError;

@RestControllerAdvice
public class ManejadorGlobalErrores {

    @ExceptionHandler(LibroNoEncontradoException.class)
    public ProblemDetail manejarLibroNoEncontrado(LibroNoEncontradoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setTitle("Libro no encontrado");
        return problema;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalle);
        problema.setTitle("Datos de entrada no válidos");
        problema.setInstance(URI.create(request.getRequestURI()));
        return problema;
    }
}
```

(El manejador se muestra con el `.sorted(...)` de la Práctica 6.3 ya incluido: las
prácticas de este capítulo se acumulan sobre el mismo método.)

### Cómo se resuelve `HttpServletRequest` sin declararlo aparte

Igual que `UriComponentsBuilder` en la Práctica 5.2, `HttpServletRequest` es un tipo que
Spring MVC sabe inyectar automáticamente como parámetro de un método manejador (de
`@ExceptionHandler` o de `@RequestMapping`), sin necesidad de configurarlo como bean ni
de pedirlo por constructor: Spring detecta el tipo del parámetro y lo resuelve con la
petición HTTP real que está en curso en ese momento.

### Comprobación

Con la misma petición inválida de la Práctica 6.3, a `POST /libros`:

```json
{
  "type": "about:blank",
  "title": "Datos de entrada no válidos",
  "status": 400,
  "detail": "anio: El año no puede ser posterior a 2100; autor: El autor es obligatorio; titulo: El título es obligatorio",
  "instance": "/libros"
}
```

`instance` queda con `/libros` — la ruta exacta a la que se hizo la petición que
desencadenó el error, tal como la devuelve `request.getRequestURI()` (la ruta, sin el
host ni el *query string*). Si la misma validación fallara en un `PUT /libros/1`,
`instance` sería `/libros/1`, no `/libros`: el valor depende de qué endpoint concreto
recibió la petición inválida, no es un texto fijo. Con esto, `ManejadorGlobalErrores`
usa ya los cuatro campos principales del RFC 9457 con contenido propio del proyecto:
`type` (en el manejador de la Práctica 6.2), `title`, `status` y ahora `instance` —
`detail` ya se rellenaba desde el principio del capítulo.
