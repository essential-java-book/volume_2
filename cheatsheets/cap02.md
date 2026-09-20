# Cheat sheet · Capítulo 2 — Primera aplicación Spring Boot

*Java Esencial · Volumen 2: Spring Boot · código del capítulo: tag `v2-cap02`*

## En una frase

Generas el proyecto `biblioteca-spring` con Spring Initializr, lo arrancas, lees su log y publicas tus dos primeros endpoints GET: uno que devuelve texto y otro que devuelve un `record` como JSON.

## Lo esencial

| Elemento | Para qué sirve |
|---|---|
| Spring Initializr (start.spring.io) | Genera el proyecto: Maven, Java 21, Group `com.javaesencial`, Artifact `biblioteca-spring`, Jar, dependencia Spring Web |
| `BibliotecaApplication` | Punto de entrada; su `main` llama a `SpringApplication.run(...)`. No se modifica |
| `@SpringBootApplication` | Combina `@SpringBootConfiguration`, `@EnableAutoConfiguration` y `@ComponentScan` |
| `@EnableAutoConfiguration` | Configura beans según las dependencias del classpath (Tomcat, Spring MVC, Jackson) |
| `@ComponentScan` | Escanea el paquete de la clase principal y sus subpaquetes en busca de beans |
| `@RestController` | Marca la clase como controlador REST; el valor de retorno va al cuerpo de la respuesta |
| `@GetMapping("/ruta")` | Mapea las peticiones GET de esa URL al método |
| Jackson | Convierte objetos Java a JSON; viene con `spring-boot-starter-web`. Un `String` sale como `text/plain` |
| `record` | Clase de datos inmutable con constructor, getters, `equals`, `hashCode` y `toString`; las claves JSON siguen el orden de sus parámetros |
| `application.properties` | Configuración externalizada; de momento vacío |
| `pruebas.http` | Peticiones del cliente HTTP de IntelliJ; va en la raíz, junto al `pom.xml` |
| `mvnw` / `mvnw.cmd` | Wrappers de Maven: ejecutan Maven sin tenerlo instalado |
| Log de arranque | `No active profile set` (perfil), `Tomcat initialized with port 8080` (puerto), `Started BibliotecaApplication in X seconds` (lista) |

## Código mínimo

La clase principal que genera Initializr:

```java
package com.javaesencial.biblioteca;

@SpringBootApplication
public class BibliotecaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibliotecaApplication.class, args);
    }
}
```

El controlador con los dos endpoints y el `record` que se serializa a JSON:

```java
@RestController
public class HolaMundoController {

    @GetMapping("/hola")
    public String hola() {
        return "Hola desde la Biblioteca Municipal \"El Quijote\"";
    }

    @GetMapping("/libro-ejemplo")
    public LibroRespuesta libroEjemplo() {
        return new LibroRespuesta(1L, "El Quijote", "Miguel de Cervantes", 1605);
    }
}

public record LibroRespuesta(Long id, String titulo, String autor, int anio) {}
```

## Comandos y peticiones

```http
### Arranca antes con: mvn spring-boot:run
### Desde la terminal: curl http://localhost:8080/hola

### Capítulo 2 -- primeras peticiones
GET http://localhost:8080/hola

###
GET http://localhost:8080/libro-ejemplo
```

## Errores típicos

- **El endpoint devuelve 404** → falta `@RestController` en la clase, falta `@GetMapping("/hola")` en el método o el controlador está fuera de `com.javaesencial.biblioteca` y sus subpaquetes. Muévelo dentro.
- **`@GetMapping("hola")` sin barra** → escribe siempre `@GetMapping("/hola")`: el path queda explícitamente absoluto y se comporta igual en todos los entornos.
- **La respuesta es `{}` y no hay error en consola** → la clase no tiene getters ni campos públicos. Usa un `record` o añade getters.
- **Sigue respondiendo el código anterior (o 404 tras añadir un controlador)** → Spring Boot no recarga solo: detén y vuelve a arrancar la aplicación. Con `spring-boot-devtools` se reinicia al detectar cambios.
- **Las claves JSON salen en un orden inesperado** → siguen el orden de los parámetros del `record`. No es un error; si necesitas otro orden, anota con `@JsonPropertyOrder({"titulo", "autor"})`.

## En el Proyecto Biblioteca

Nace el proyecto `biblioteca-spring`, nuevo y separado del `biblioteca` del Volumen 1, que queda intacto como referencia. Todo vive en el paquete `com.javaesencial.biblioteca`: `BibliotecaApplication` (generada), `HolaMundoController` con `GET /hola` y `GET /libro-ejemplo`, el `record` `LibroRespuesta` y el fichero `pruebas.http`. `application.properties` sigue vacío hasta el Capítulo 4.
