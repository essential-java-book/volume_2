# Práctica 2.1 — Cambiar el puerto y añadir información de arranque

## Enunciado

Abre `src/main/resources/application.properties` y añade:

```properties
server.port=9090
spring.application.name=biblioteca-spring
```

1. Reinicia la aplicación. ¿En qué puerto escucha ahora? ¿Aparece el nombre de la
   aplicación en el log?
2. Prueba que `http://localhost:9090/hola` responde correctamente.
3. ¿Por qué tiene sentido externalizar el puerto en `application.properties` en lugar de
   fijarlo en el código?

## Solución

`src/main/resources/application.properties` completo (en el tag `v2-cap02` el fichero
está vacío, así que estas dos líneas son todo su contenido):

```properties
server.port=9090
spring.application.name=biblioteca-spring
```

No hay que tocar ni una línea de Java: Spring Boot lee este fichero al arrancar y
configura el Tomcat embebido con esos valores.

### 1. Puerto y nombre en el log

Ahora escucha en el **9090**. Se ve en la línea del log que anuncia el servidor web:

```text
Tomcat started on port 9090 (http) with context path '/'
```

El nombre también aparece. Desde Spring Boot 3.2, cuando `spring.application.name` tiene
valor, el formato de log por defecto lo incluye entre corchetes en **todas** las líneas,
justo antes del nombre del hilo:

```text
2026-09-20T10:15:42.318+02:00  INFO 18244 --- [biblioteca-spring] [           main] c.j.biblioteca.BibliotecaApplication     : Started BibliotecaApplication in 1.412 seconds (process running for 1.83)
```

(La fecha, el número de proceso y los tiempos serán distintos en tu máquina.) Sin la
propiedad, ese primer par de corchetes sencillamente no está. Si en algún momento
molesta, se quita con `logging.include-application-name=false`.

El nombre no es solo decorativo: en el Volumen 3 será el identificador con el que cada
microservicio se registra en Eureka, así que conviene acostumbrarse a darle siempre un
valor.

### 2. Comprobar `/hola` en el puerto nuevo

```http
GET http://localhost:9090/hola
```

Respuesta, `200 OK`:

```text
Hola desde la Biblioteca Municipal "El Quijote"
```

Y la comprobación inversa: `http://localhost:8080/hola` ya **no** responde (el navegador
muestra «no se puede conectar» y `curl` devuelve `Connection refused`), porque en el 8080
ya no hay nadie escuchando. El puerto no se añade: se sustituye.

> **Antes de seguir con el libro**, vuelve a dejar el puerto en 8080 (borra la línea
> `server.port` o ponle `8080`). El resto del capítulo, el fichero `pruebas.http` del
> repositorio y todos los capítulos siguientes usan `http://localhost:8080`. La línea
> `spring.application.name` puedes dejarla.

### 3. Por qué externalizar el puerto

Porque el puerto no es lógica del programa, es una decisión del **entorno** donde se
ejecuta, y el mismo programa se ejecuta en varios entornos:

- En tu portátil te vale el 8080, salvo el día que lo tenga ocupado otra aplicación. Con
  el puerto en un fichero de propiedades lo cambias en diez segundos; escrito en el
  código tendrías que modificar una clase, recompilar y volver a empaquetar.
- El **mismo JAR** debe poder arrancar en desarrollo, en pruebas y en producción sin
  recompilarse. Spring Boot permite sobrescribir cualquier propiedad desde fuera del JAR,
  por orden de prioridad creciente: `application.properties`, variable de entorno
  (`SERVER_PORT=9090`) y argumento de línea de comandos
  (`java -jar biblioteca-spring-1.0.0.jar --server.port=9090`).
- Plataformas como Railway o Render, y los contenedores Docker del Capítulo 15,
  **imponen** el puerto mediante una variable de entorno. Una aplicación con el puerto
  fijado en el código no se puede desplegar ahí.

La regla general: lo que cambia de una máquina a otra va fuera del código. El Capítulo 4
la lleva más lejos con `@Value`, `@ConfigurationProperties` y los perfiles `dev` y `prod`.
