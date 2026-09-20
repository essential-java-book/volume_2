# Práctica 6.2 — Manejador para `IllegalArgumentException`

## Enunciado

Algunos métodos del servicio pueden lanzar `IllegalArgumentException` cuando reciben
argumentos fuera de rango. Añade a `ManejadorGlobalErrores` un método que:

- Capture `IllegalArgumentException`.
- Devuelva un `ProblemDetail` con estado 400 y el mensaje de la excepción como `detail`.
- Use el `type` `https://biblioteca.javaesencial.com/errores/argumento-invalido`.

## Solución

`ManejadorGlobalErrores` con el tercer manejador añadido, junto a los dos que ya existen
(`manejarLibroNoEncontrado` y `manejarValidacion`):

```java
package com.javaesencial.biblioteca.controlador;

import com.javaesencial.biblioteca.dominio.LibroNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail manejarArgumentoInvalido(IllegalArgumentException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problema.setTitle("Argumento no válido");
        problema.setType(URI.create("https://biblioteca.javaesencial.com/errores/argumento-invalido"));
        return problema;
    }
}
```

### Cómo se comprueba

Ninguno de los métodos actuales de `BibliotecaService` lanza `IllegalArgumentException`
todavía (los dos manejadores existentes cubren "no encontrado" y "validación de Bean
Validation"), así que para probar este tercero hace falta provocarlo a propósito, por
ejemplo añadiendo temporalmente una comprobación a `BibliotecaService.registrar`:

```java
public Libro registrar(Libro libro) {
    if (libro.getAnio() != null && libro.getAnio() > java.time.Year.now().getValue()) {
        throw new IllegalArgumentException("El año no puede ser posterior al año actual");
    }
    Libro creado = repositorio.agregar(libro);
    log.info("Libro registrado: {} (id={})", creado.getTitulo(), creado.getId());
    return creado;
}
```

Con esa comprobación temporal, un `POST /libros` con un año futuro respondería:

```terminal
HTTP/1.1 400
```
```json
{
  "type": "https://biblioteca.javaesencial.com/errores/argumento-invalido",
  "title": "Argumento no válido",
  "status": 400,
  "detail": "El año no puede ser posterior al año actual"
}
```

Nótese la diferencia con `manejarLibroNoEncontrado` y `manejarValidacion`, que dejan
`type` en su valor por defecto (`"about:blank"`, el valor estándar del RFC 9457 cuando no
se personaliza): aquí `setType(...)` sí se llama explícitamente, con una URI propia del
proyecto que identifica esta categoría concreta de error — el propio RFC 9457 permite
(y anima a) definir URIs de este estilo para que un cliente de la API pueda, si quiere,
distinguir programáticamente entre categorías de error por su `type` en vez de solo por
el texto de `detail`.
