# Práctica 5.4 — Campo `ultimaModificacion` en `PUT`

## Enunciado

Añade un campo `ultimaModificacion` de tipo `LocalDateTime` a la clase `Libro`:

```java
private LocalDateTime ultimaModificacion;
// getter y setter correspondientes
```

En `RepositorioLibros.actualizar()`, asigna `LocalDateTime.now()` al campo justo antes de
devolver el libro actualizado.

1. Realiza un `PUT /libros/1` y comprueba que la respuesta JSON incluye
   `ultimaModificacion`.
2. ¿Debería el cliente poder enviar `ultimaModificacion` en el cuerpo del `PUT`? ¿Qué
   problema podría causar?
3. ¿Cómo podrías evitar que Jackson serialice ese campo cuando el cliente envía el JSON
   de entrada? (Pista: investiga `@JsonIgnore` y recuerda que en el Capítulo 6 aprenderás
   a separar los modelos de entrada y salida.)

## Solución

`Libro` con el campo nuevo:

```java
package com.javaesencial.biblioteca.dominio;

import java.time.LocalDateTime;

public class Libro {

    private Long id;
    private String titulo;
    private String autor;
    private int anio;
    private LocalDateTime ultimaModificacion;

    public Libro() {
    }

    public Libro(Long id, String titulo, String autor, int anio) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.anio = anio;
    }

    // getters y setters de id, titulo, autor, anio: sin cambios

    public LocalDateTime getUltimaModificacion() {
        return ultimaModificacion;
    }

    public void setUltimaModificacion(LocalDateTime ultimaModificacion) {
        this.ultimaModificacion = ultimaModificacion;
    }
}
```

`RepositorioLibros.actualizar()` con la asignación añadida:

```java
public Optional<Libro> actualizar(Long id, Libro datos) {
    return findById(id).map(libro -> {
        libro.setTitulo(datos.getTitulo());
        libro.setAutor(datos.getAutor());
        libro.setAnio(datos.getAnio());
        libro.setUltimaModificacion(LocalDateTime.now());
        return libro;
    });
}
```

(`BibliotecaService.actualizar` y `LibroController.actualizar` no cambian: ambos ya
delegan sin conocer los campos concretos de `Libro`.)

### 1. `PUT /libros/1` y el JSON de respuesta

```json
{
  "id": 1,
  "titulo": "El Quijote",
  "autor": "Miguel de Cervantes",
  "anio": 1605,
  "ultimaModificacion": "2026-09-11T10:32:07.481"
}
```

`ultimaModificacion` aparece con la fecha y hora exactas en las que se ejecutó el `PUT`
(formato ISO-8601, la serialización por defecto que Jackson aplica a `LocalDateTime`
cuando el proyecto incluye el módulo `jackson-datatype-jsr310` —incluido automáticamente
por `spring-boot-starter-web`—).

### 2. ¿Debería el cliente poder enviarlo? ¿Qué problema causaría?

No debería. Si el cliente pudiera mandar `ultimaModificacion` en el cuerpo del `PUT`,
podría escribir un valor arbitrario —una fecha futura, una fecha pasada falsa, o
simplemente un valor inconsistente con la hora real del servidor— y, tal como está
`actualizar()` ahora mismo, ese valor **de entrada no se usa** (el método fija siempre
`LocalDateTime.now()`, ignorando lo que venga en `datos`), así que hoy no hay problema
real. Pero es un campo que, conceptualmente, **no debería depender de lo que decida
enviar el cliente**: es información que el propio servidor calcula y controla (como el
`id`, que tampoco debería poder fijarlo el cliente), no un dato de negocio que el usuario
introduce. Aceptarlo silenciosamente del cliente sin usarlo también es confuso desde
fuera: alguien integrando con la API podría pensar, razonablemente, que enviar ese campo
sí tiene efecto.

### 3. Evitar que Jackson lo acepte en la entrada

La pista del enunciado apunta a la solución correcta, que además es la que se generaliza
en el Capítulo 6: usar **dos modelos distintos**, uno de entrada y otro de salida, en vez
de reutilizar la misma clase `Libro` para las dos direcciones. Mientras se llega a esa
separación completa, una solución parcial válida con la misma clase `Libro` es anotar el
setter con `@JsonProperty(access = JsonProperty.Access.READ_ONLY)` (más preciso aquí que
`@JsonIgnore`, que ignoraría el campo en los dos sentidos):

```java
@com.fasterxml.jackson.annotation.JsonProperty(access =
        com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
public LocalDateTime getUltimaModificacion() {
    return ultimaModificacion;
}
```

`READ_ONLY` le dice a Jackson: incluye este campo cuando **serializas** (lo que el
servidor devuelve), pero ignóralo por completo si aparece en el JSON de **entrada** (lo
que el cliente envía) — exactamente el comportamiento que pide la pregunta 2: el campo
sigue apareciendo en la respuesta, pero un cliente que intente fijarlo en el cuerpo de la
petición no tiene ningún efecto, silenciosamente. La solución definitiva, con DTOs de
entrada y salida separados (`LibroPeticion`/`LibroRespuesta` o similar), evita este tipo
de anotaciones por completo: el DTO de entrada, directamente, no declara ese campo, así
que no hay forma de que el cliente lo envíe ni ambigüedad que resolver.
