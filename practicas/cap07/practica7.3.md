# Práctica 7.3 — Deshabilitar Swagger en producción

## Enunciado

Crea el fichero `application-prod.properties` (o modifica el existente) para
deshabilitar tanto Swagger UI como la ruta `/v3/api-docs` en el perfil de producción.
Verifica que al arrancar con `--spring.profiles.active=prod` la URL `/docs` devuelve 404
y que `/v3/api-docs` también.

## Solución

`application-prod.properties`, tal como queda al terminar el cuerpo del capítulo (sin
ninguna propiedad de `springdoc` todavía):

```properties
server.port=${PORT:8080}
logging.level.com.javaesencial.biblioteca=WARN

biblioteca.nombre=Biblioteca Municipal "El Quijote"
biblioteca.version=2.0
biblioteca.max-libros=500
biblioteca.modo-mantenimiento=false
```

Con las dos propiedades de la práctica añadidas:

```properties
server.port=${PORT:8080}
logging.level.com.javaesencial.biblioteca=WARN

biblioteca.nombre=Biblioteca Municipal "El Quijote"
biblioteca.version=2.0
biblioteca.max-libros=500
biblioteca.modo-mantenimiento=false

springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false
```

(`application-dev.properties` no necesita ningún cambio: `springdoc.swagger-ui.enabled` y
`springdoc.api-docs.enabled` valen `true` por defecto si no se indica lo contrario, así
que en desarrollo Swagger sigue disponible sin tocar nada — la práctica solo pide
deshabilitarlo en `prod`, no reafirmarlo explícitamente en `dev`.)

### Comprobación con `--spring.profiles.active=prod`

```terminal
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

```terminal
GET http://localhost:8080/docs
```
```terminal
HTTP/1.1 404
```

```terminal
GET http://localhost:8080/v3/api-docs
```
```terminal
HTTP/1.1 404
```

Las dos rutas responden **404**. Con `springdoc.swagger-ui.enabled=false`, springdoc ni
siquiera registra el controlador que sirve la interfaz de Swagger UI en `/docs`
(la ruta personalizada de `application.properties`, `springdoc.swagger-ui.path=/docs`);
con `springdoc.api-docs.enabled=false`, tampoco registra el endpoint que genera la
especificación OpenAPI en JSON, `/v3/api-docs`. Al no existir ningún *handler* registrado
para esas rutas, Spring responde con el mismo 404 genérico que cualquier ruta
inexistente — no es un 403 (prohibido) ni un mensaje especial de "Swagger deshabilitado":
desde fuera, es indistinguible de una URL que nunca hubiera existido, que es justo el
efecto de seguridad que persigue el "Error 5" del propio capítulo (no dar pistas de que
ahí había algo).

Arrancando en cambio con el perfil `dev` (`spring.profiles.active=dev`, el de siempre),
`/docs` y `/v3/api-docs` siguen respondiendo con normalidad, sin ningún cambio de
comportamiento: la diferencia de configuración entre perfiles, una vez más, se resuelve
con qué fichero de propiedades se carga al arrancar, no con ninguna condición dentro del
código.
