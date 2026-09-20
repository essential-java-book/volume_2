# Práctica 2.2 — Segundo endpoint con parámetro en la URL

## Enunciado

Añade a `HolaMundoController` un endpoint que salude al nombre recibido:

```java
@GetMapping("/hola/{nombre}")
public String saludar(@PathVariable String nombre) {
    return "¡Hola, " + nombre + "! Bienvenido a la Biblioteca.";
}
```

1. Reinicia y prueba con `GET http://localhost:8080/hola/Basilio`.
2. ¿Qué devuelve si el nombre contiene espacios, como `Ana María`? ¿Cómo se codifica en
   la URL?
3. ¿Qué ocurre si accedes a `GET http://localhost:8080/hola/` sin nombre? ¿Qué código de
   estado devuelve?

## Solución

`HolaMundoController` con el endpoint añadido. Lo único nuevo, además del método, es el
`import` de `PathVariable`:

```java
package com.javaesencial.biblioteca;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HolaMundoController {

    @GetMapping("/hola")
    public String hola() {
        return "Hola desde la Biblioteca Municipal \"El Quijote\"";
    }

    @GetMapping("/hola/{nombre}")
    public String saludar(@PathVariable String nombre) {
        return "¡Hola, " + nombre + "! Bienvenido a la Biblioteca.";
    }

    @GetMapping("/libro-ejemplo")
    public LibroRespuesta libroEjemplo() {
        return new LibroRespuesta(1L, "El Quijote", "Miguel de Cervantes", 1605);
    }
}
```

`/hola` y `/hola/{nombre}` conviven sin conflicto: son dos rutas distintas, porque la
segunda exige un segmento más después de la barra.

### 1. `GET /hola/Basilio`

```http
GET http://localhost:8080/hola/Basilio
```

Respuesta, `200 OK`:

```text
¡Hola, Basilio! Bienvenido a la Biblioteca.
```

Spring toma el segmento de la URL que ocupa el lugar de `{nombre}` y lo pasa como
argumento al parámetro anotado con `@PathVariable`. Se emparejan por nombre: si el
parámetro se llamara de otra forma habría que indicarlo, `@PathVariable("nombre") String
quien`.

### 2. Un nombre con espacios: `Ana María`

Devuelve el saludo con el nombre tal cual, con su espacio y su tilde:

```text
¡Hola, Ana María! Bienvenido a la Biblioteca.
```

Lo que cambia es cómo viaja. Una URL no puede contener espacios ni caracteres que no sean
ASCII, así que se sustituyen por `%` seguido del valor hexadecimal de cada byte
(*percent-encoding*): el espacio es `%20` y la `í`, que en UTF-8 ocupa dos bytes, es
`%C3%AD`. La petición real es:

```http
GET http://localhost:8080/hola/Ana%20Mar%C3%ADa
```

El navegador, Postman y el cliente HTTP de IntelliJ hacen esa conversión solos cuando
escribes `Ana María` en la barra de direcciones. Con `curl` hay que escribirla ya
codificada:

```bash
curl http://localhost:8080/hola/Ana%20Mar%C3%ADa
```

En el otro extremo, Spring **descodifica** el segmento antes de entregarlo al método, y
por eso `nombre` contiene `Ana María` y no `Ana%20Mar%C3%ADa`. (Si en la consola de
Windows ves `MarÃ­a`, la respuesta es correcta: es la consola la que no está mostrando
UTF-8.)

Un detalle: en la *ruta* de una URL el espacio es `%20`. El signo `+` como espacio solo
vale en los parámetros de consulta (`?nombre=Ana+María`); `/hola/Ana+María` saludaría
literalmente a «Ana+María».

### 3. `GET /hola/` sin nombre

Devuelve **404 Not Found**, con el cuerpo de error por defecto de Spring Boot:

```json
{
  "timestamp": "2026-09-20T08:15:42.318+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/hola/"
}
```

(Ese JSON es lo que reciben Postman, `curl` o el cliente HTTP de IntelliJ. El navegador,
como pide HTML, ve la misma información en la página *Whitelabel Error Page*.)

Dos motivos, uno por cada ruta que podría haber encajado:

- **No encaja con `/hola/{nombre}`**, porque una variable de ruta necesita al menos un
  carácter. Un segmento vacío no cuenta como valor, así que el método `saludar` ni
  siquiera llega a ejecutarse (no recibe `""` ni `null`).
- **No encaja con `/hola`**, porque desde Spring Framework 6 (el de Spring Boot 3) la
  barra final ya no se ignora: `/hola` y `/hola/` son rutas distintas. En Spring Boot 2
  esta misma petición habría devuelto el saludo genérico; es un cambio que despista al
  leer tutoriales antiguos.

Si quisieras que `/hola/` respondiera, habría que declararlo expresamente, por ejemplo
`@GetMapping({"/hola", "/hola/"})` en el método `hola()`.
