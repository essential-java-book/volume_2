# Práctica 2.3 — Record con más campos

## Enunciado

Crea un record `AutorRespuesta(String nombre, String nacionalidad, int anioNacimiento)` y
añade un endpoint `GET /autor-ejemplo` que devuelva un autor inventado.

1. ¿Cómo aparecen los tres campos en el JSON? ¿Coinciden los nombres con los del record?
2. Cambia el nombre del campo `anioNacimiento` a `anio_nacimiento` usando la anotación
   `@JsonProperty("anio_nacimiento")`. ¿Qué ocurre en el JSON?
3. ¿Cuándo tiene sentido usar `@JsonProperty` para cambiar el nombre de una clave JSON?

## Solución

El record, en un fichero nuevo `AutorRespuesta.java`, en el mismo paquete que
`LibroRespuesta`:

```java
package com.javaesencial.biblioteca;

public record AutorRespuesta(String nombre, String nacionalidad, int anioNacimiento) {
}
```

Y el endpoint, añadido a `HolaMundoController` a continuación de `libroEjemplo()`:

```java
    @GetMapping("/autor-ejemplo")
    public AutorRespuesta autorEjemplo() {
        return new AutorRespuesta("Elena Garrido", "española", 1972);
    }
```

No hace falta ningún `import` nuevo en el controlador, porque el record está en su mismo
paquete.

### 1. Los tres campos en el JSON

```http
GET http://localhost:8080/autor-ejemplo
```

Respuesta, `200 OK`, con `Content-Type: application/json`:

```json
{"nombre":"Elena Garrido","nacionalidad":"española","anioNacimiento":1972}
```

Los nombres coinciden exactamente con los componentes del record, respetando mayúsculas
y minúsculas (`anioNacimiento`, en *camelCase*), y salen en el orden en que se
declararon. Los dos `String` van entre comillas y el `int` sale como número JSON, sin
comillas. Jackson obtiene todo eso de los componentes del record: no has escrito ni un
*getter* ni una línea de configuración.

### 2. Con `@JsonProperty("anio_nacimiento")`

La anotación se pone delante del componente, dentro de la cabecera del record:

```java
package com.javaesencial.biblioteca;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AutorRespuesta(
        String nombre,
        String nacionalidad,
        @JsonProperty("anio_nacimiento") int anioNacimiento) {
}
```

El controlador no cambia. La respuesta ahora es:

```json
{"nombre":"Elena Garrido","nacionalidad":"española","anio_nacimiento":1972}
```

La clave se **renombra**, no se duplica: `anioNacimiento` desaparece del JSON y en su
lugar está `anio_nacimiento`. En Java no cambia nada: el componente sigue llamándose
`anioNacimiento` y se sigue leyendo con `autor.anioNacimiento()`. La anotación solo
afecta a la traducción entre el objeto y el JSON, y en los dos sentidos: si más adelante
este record se recibiera en un `POST`, Jackson también buscaría la clave
`anio_nacimiento` en el cuerpo de la petición.

`@JsonProperty` viene con Jackson, que ya está en el proyecto porque lo trae
`spring-boot-starter-web`. No hay que añadir nada al `pom.xml`.

### 3. Cuándo tiene sentido `@JsonProperty`

Cuando el nombre que necesita el JSON y el nombre correcto en Java no pueden ser el
mismo:

- **El contrato lo fija otro.** Un frontend, una aplicación móvil o una API externa ya
  acordada esperan *snake_case* (`anio_nacimiento`), mientras que la convención de Java
  es *camelCase*. La anotación deja que cada lado siga su convención.
- **Quieres renombrar en Java sin romper a los clientes.** Si mañana el componente pasa a
  llamarse `nacimiento`, con `@JsonProperty("anio_nacimiento")` el JSON publicado sigue
  igual y nadie que consuma la API se entera.
- **La clave JSON no es un identificador Java válido**: `"fecha-alta"`, `"@id"`, o una
  palabra reservada como `"class"` o `"default"`.

Y cuándo **no**: si quieres *snake_case* en toda la API, anotar campo por campo es
repetitivo y fácil de olvidar en alguno. Para eso hay una propiedad global,
`spring.jackson.property-naming-strategy=SNAKE_CASE`, que lo aplica a todas las
respuestas. `@JsonProperty` es para las excepciones, no para la regla.
