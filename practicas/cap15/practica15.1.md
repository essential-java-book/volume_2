# Práctica 15.1 — Health check personalizado

## Enunciado

Spring Boot Actuator permite crear tus propios indicadores de salud. Implementa un
`HealthIndicator` personalizado llamado `AlmacenamientoHealthIndicator` que compruebe si
hay suficiente espacio en disco para almacenar los libros digitales:

1. Crea la clase `AlmacenamientoHealthIndicator` anotada con `@Component` que implemente
   `HealthIndicator`.
2. En el método `health()`, comprueba el espacio libre disponible en el directorio de
   trabajo.
3. Si hay más de 100 MB libres, devuelve
   `Health.up().withDetail("espacioLibre", espacioMB + " MB").build()`.
4. Si hay menos de 100 MB, devuelve
   `Health.down().withDetail("motivo", "Espacio en disco insuficiente").build()`.
5. Verifica que el nuevo indicador aparece en `/actuator/health` cuando el usuario es
   `ROLE_ADMIN`.

## Solución

`AlmacenamientoHealthIndicator`, en el paquete `configuracion` (junto a `BibliotecaConfig`
y `SeguridadConfig`, la infraestructura de la aplicación que no pertenece a ninguna capa
de dominio/controlador/servicio):

```java
package com.javaesencial.biblioteca.configuracion;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Comprueba que el directorio de trabajo tiene espacio suficiente
 * para seguir almacenando los libros digitales. Aparece en
 * /actuator/health como el componente "almacenamiento" -- Spring
 * Boot deriva el nombre del bean quitando el sufijo
 * "HealthIndicator" y pasando a camelCase la primera letra.
 */
@Component
public class AlmacenamientoHealthIndicator implements HealthIndicator {

    private static final long UMBRAL_MINIMO_BYTES = 100L * 1024 * 1024; // 100 MB

    @Override
    public Health health() {
        File directorioTrabajo = new File(".");
        long espacioLibreBytes = directorioTrabajo.getUsableSpace();
        long espacioLibreMB = espacioLibreBytes / (1024 * 1024);

        if (espacioLibreBytes >= UMBRAL_MINIMO_BYTES) {
            return Health.up()
                    .withDetail("espacioLibre", espacioLibreMB + " MB")
                    .build();
        }

        return Health.down()
                .withDetail("motivo", "Espacio en disco insuficiente")
                .withDetail("espacioLibre", espacioLibreMB + " MB")
                .build();
    }
}
```

`File.getUsableSpace()` (no `getFreeSpace()`) es la elección correcta aquí: `getFreeSpace()`
devuelve el espacio libre bruto de la partición, mientras que `getUsableSpace()` tiene en
cuenta permisos y cuotas del sistema operativo —el espacio que la propia JVM podría llegar
a usar realmente—, que es lo relevante para decidir si la aplicación puede seguir
escribiendo datos.

### Por qué solo `ROLE_ADMIN` ve el detalle en `/actuator/health`

`/actuator/health` ya es una ruta **pública** en `SecurityFilterChain` desde el cuerpo del
capítulo (`.requestMatchers("/actuator/health", "/actuator/info").permitAll()`) — el
healthcheck de Docker la necesita sin credenciales. Pero "pública" y "con todos los
detalles visibles" son dos cosas distintas: Actuator tiene su **propia** capa de
autorización, independiente de las reglas de Spring Security, controlada por
`management.endpoint.health.show-details` (ya configurada en `application.properties`
como `when-authorized`). Falta un paso para que "autorizado" signifique específicamente
`ROLE_ADMIN` y no "cualquier usuario autenticado": añadir

```properties
management.endpoint.health.roles=ADMIN
```

Sin esta propiedad, `show-details=when-authorized` sin `roles` configurado solo comprueba
que haya alguien autenticado (no exige ningún rol concreto) — con `roles=ADMIN`, Actuator
comprueba explícitamente `request.isUserInRole("ADMIN")` antes de incluir el desglose de
componentes (incluido `almacenamiento`) en la respuesta.

### Comprobación

Sin autenticar (público, pero sin detalle):

```terminal
GET /actuator/health
```
```json
{ "status": "UP" }
```

Con un token JWT de un usuario `ROLE_ADMIN` (Capítulo 14):

```terminal
GET /actuator/health
Authorization: Bearer eyJhbGci...
```
```json
{
  "status": "UP",
  "components": {
    "almacenamiento": {
      "status": "UP",
      "details": { "espacioLibre": "42381 MB" }
    },
    "db": {
      "status": "UP",
      "details": { "database": "PostgreSQL", "validationQuery": "isValid()" }
    },
    "diskSpace": {
      "status": "UP",
      "details": { "total": 494384795648, "free": 445123456789, "threshold": 10485760 }
    },
    "ping": { "status": "UP" }
  }
}
```

`almacenamiento` aparece junto a los indicadores que Spring Boot ya registra
automáticamente (`db`, `diskSpace`, `ping`...) sin ningún registro manual adicional:
cualquier bean que implemente `HealthIndicator` se agrega solo al agregado de
`/actuator/health`, que es precisamente el mecanismo de extensión que pedía el enunciado.
