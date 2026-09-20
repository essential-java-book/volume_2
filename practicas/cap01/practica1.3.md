# Práctica 1.3 — Primer arranque y lectura del log

## Enunciado

Con el proyecto de la Práctica 1.1, ejecuta:

```terminal
mvn spring-boot:run
```

Observa la consola y localiza:

1. La línea que indica qué perfil está activo (o que no hay ninguno activo).
2. La línea que indica en qué puerto escucha Tomcat.
3. La línea que indica cuántos segundos tardó en arrancar.
4. El nombre de la clase que Spring Boot llama al inicio.

Después abre un navegador y accede a `http://localhost:8080`. ¿Qué ves? ¿Es un error? ¿Por
qué? (Pista: todavía no hemos creado ningún controlador.)

## Solución

Al ejecutar `mvn spring-boot:run` sobre el proyecto recién generado (sin ningún
controlador todavía), la consola muestra el *banner* de Spring Boot y, a continuación,
una serie de líneas de log con marca de tiempo. Con la configuración por defecto (sin
`application.properties` tocado todavía, como quedó en la Práctica 1.1), las cuatro
líneas que pide el enunciado son estas (formato real de Spring Boot 3.3.x con Tomcat
embebido; la marca de tiempo y el PID cambian en cada ejecución):

```terminal
2026-09-10T10:15:32.104+02:00  INFO 8421 --- [           main] c.j.biblioteca.BibliotecaSpringApplication : No active profile set, falling back to 1 default profile: "default"
2026-09-10T10:15:32.980+02:00  INFO 8421 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer : Tomcat initialized with port 8080 (http)
2026-09-10T10:15:33.892+02:00  INFO 8421 --- [           main] c.j.biblioteca.BibliotecaSpringApplication : Started BibliotecaSpringApplication in 1.234 seconds (process running for 1.512)
```

1. **Perfil activo:** `No active profile set, falling back to 1 default profile:
   "default"`. Como no se ha definido ningún `application-<perfil>.properties` ni la
   propiedad `spring.profiles.active`, Spring Boot usa el perfil implícito `default`
   (esto cambiará en capítulos posteriores, cuando se introduzcan perfiles reales como
   `dev`/`test`/`prod`).
2. **Puerto de Tomcat:** `Tomcat initialized with port 8080 (http)`. 8080 es el puerto
   HTTP por defecto de Spring Boot cuando no se fija `server.port` en
   `application.properties`.
3. **Tiempo de arranque:** `Started BibliotecaSpringApplication in 1.234 seconds (process
   running for 1.512)`. El primer número es el tiempo que Spring tardó en crear el
   contexto de la aplicación (desde `main` hasta que queda lista para atender
   peticiones); el segundo, entre paréntesis, es el tiempo total transcurrido desde que
   arrancó el proceso de la JVM (incluye la propia carga de la JVM, más lenta). El valor
   exacto varía de una ejecución a otra y depende del equipo, pero conceptualmente estas
   dos cifras son las que hay que localizar.
4. **Clase llamada al inicio:** `BibliotecaSpringApplication` — la misma clase con
   `@SpringBootApplication` y el `main` de la Práctica 1.1; aparece como parte del nombre
   del *logger* (`c.j.biblioteca.BibliotecaSpringApplication`, abreviatura del paquete
   `com.javaesencial.biblioteca`) tanto en la línea del perfil como en la del arranque
   completado.

### `http://localhost:8080` en el navegador

Con el servidor arrancado pero sin ningún controlador definido todavía, el navegador
muestra una página de error en formato JSON (o HTML, según el `Accept` que mande el
navegador), con este contenido aproximado:

```
{
  "timestamp": "2026-09-10T08:15:40.123+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/"
}
```

**Sí es un error, pero es el esperado.** `404 Not Found` significa que el servidor
respondió correctamente —Tomcat está arriba y funcionando, Spring procesó la petición—
pero no encontró ningún componente registrado que atienda la ruta `/`. Es la respuesta
por defecto de Spring Boot (`BasicErrorController`) cuando ninguna clase está anotada con
`@RestController`/`@Controller` para esa ruta: como dice la pista del enunciado, todavía
no se ha creado ningún controlador, así que no hay nada que pueda responder a esa
petición. Este 404 desaparece en el Capítulo 2, en cuanto se define el primer
`@RestController` con un método mapeado a `/`.
