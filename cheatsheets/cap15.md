# Cheat sheet · Capítulo 15 — Actuator, métricas y Docker

*Java Esencial · Volumen 2: Spring Boot · código del capítulo: tag `v2-cap15`*

## En una frase

Sabes dejar la API lista para producción: expones salud, información y métricas con Actuator (protegidas con Spring Security), separas la configuración con el perfil `prod` y variables de entorno, y levantas la API junto a PostgreSQL con un `Dockerfile` multicapa y un solo `docker compose up`.

## Lo esencial

| Elemento | Para qué sirve |
|---|---|
| `spring-boot-starter-actuator` | Añade endpoints de operaciones bajo `/actuator`; por defecto solo `health` e `info` se exponen sobre HTTP |
| `/actuator/health` | Estado de la aplicación y sus dependencias; devuelve 503 si la base de datos cae. Lo usan balanceadores, Kubernetes y el `HEALTHCHECK` de Docker |
| `management.endpoints.web.exposure.include` | Lista de endpoints expuestos; en el proyecto, `health,info,metrics` |
| `management.endpoint.health.show-details=when-authorized` | El detalle de cada componente (`db`, `diskSpace`) solo se muestra a usuarios autorizados |
| `info.app.*` | Propiedades que devuelve `/actuator/info` (nombre, versión, descripción) |
| Perfiles `dev` / `prod` | `application-dev.properties` (H2) y `application-prod.properties` (PostgreSQL, valores por variable de entorno) |
| `SPRING_PROFILES_ACTIVE` | Variable de entorno que elige el perfil; sin ella manda `spring.profiles.active=dev` |
| Imagen / contenedor | Plantilla inmutable con todo lo necesario / instancia en ejecución de esa imagen |
| Dockerfile en tres etapas | Compila con Maven, extrae las capas con `-Djarmode=layertools` y monta la imagen final con usuario sin privilegios |
| Capas del JAR | `dependencies`, `spring-boot-loader`, `snapshot-dependencies` y `application`: se copian de menor a mayor frecuencia de cambio para aprovechar la caché |
| `docker-compose.yml` | Define la API y PostgreSQL como un sistema; `db` es el hostname de la base de datos dentro de la red de Compose |
| `depends_on` + `condition: service_healthy` | La API espera a que PostgreSQL pase su `healthcheck` (`pg_isready`) |
| Volumen nombrado `datos_biblioteca` | Conserva los datos de PostgreSQL aunque se eliminen los contenedores |
| `-e` / `environment:` | Inyectan configuración (`DB_URL`, `DB_USUARIO`, `DB_PASSWORD`, `PORT`) sin modificar la imagen |

## Código mínimo

Las dos reglas nuevas de `SeguridadConfig`, antes de las de `/libros`: salud e info públicas, el resto de Actuator solo para `ADMIN`.

```java
.requestMatchers("/actuator/health", "/actuator/info").permitAll()
.requestMatchers("/actuator/**").hasRole("ADMIN")
```

La conexión en `application-prod.properties`: `${VARIABLE:valor-por-defecto}`.

```properties
server.port=${PORT:8080}
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/biblioteca}
spring.datasource.username=${DB_USUARIO:biblioteca_user}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
```

El servicio `api` de `docker-compose.yml` (el servicio `db` usa `postgres:16-alpine` y no publica el puerto 5432 al host):

```yaml
  api:
    build: .
    depends_on:
      db:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_URL: jdbc:postgresql://db:5432/biblioteca
      DB_USUARIO: biblioteca_user
      DB_PASSWORD: ${DB_PASSWORD:-cambia-esta-clave}
      PORT: 8080
    ports:
      - "8080:8080"
```

## Comandos y peticiones

```bash
docker build -t biblioteca-api:1.0.0 .      # construye la imagen (compila el JAR dentro)
docker compose up -d --build                # API + PostgreSQL en segundo plano
docker compose ps                           # ¿están los dos contenedores UP?
docker compose logs -f api                  # logs solo de la API
docker compose down                         # elimina contenedores; los volúmenes persisten
docker compose down -v                      # elimina también los volúmenes (borra la BD)

curl http://localhost:8080/actuator/health  # público, sin token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
curl http://localhost:8080/actuator/metrics \
  -H "Authorization: Bearer $TOKEN"         # exige token de admin
```

## Errores típicos

- **`/actuator/env` enseña secretos y contraseñas a cualquiera** → no uses `management.endpoints.web.exposure.include=*`. Expón solo `health,info,metrics` y protege el resto con `hasRole("ADMIN")`.
- **`PSQLException: Connection to db:5432 refused` al hacer `docker compose up`** → `depends_on` a secas no espera a PostgreSQL. Usa `condition: service_healthy` y un `healthcheck` con `pg_isready` en `db`.
- **La base de datos aparece vacía tras `docker compose down` y `up`** → declara un volumen nombrado (`datos_biblioteca:/var/lib/postgresql/data`). Solo `down -v` borra los volúmenes.
- **Cada `docker build` vuelve a descargar todas las dependencias** → copia `pom.xml` y ejecuta `mvn -B dependency:go-offline` antes de `COPY src/ src/`.
- **Los tests intentan conectarse a `localhost:5432`** → tienes `SPRING_PROFILES_ACTIVE=prod` definida en el sistema o en el IDE. Anota el test con `@ActiveProfiles("dev")` o crea un perfil `test` con H2.

## En el Proyecto Biblioteca

El `pom.xml` solo suma `spring-boot-starter-actuator`: el driver de PostgreSQL está desde el Capítulo 9. `SeguridadConfig` añade las reglas de `/actuator/**`, `application.properties` las propiedades `management.*` e `info.app.*`, y `application-prod.properties` pasa a leer `DB_URL` y `DB_USUARIO` del entorno. En la raíz aparecen `Dockerfile` y `docker-compose.yml`. Las migraciones V1 a V3 sirven tal cual en PostgreSQL porque usan SQL estándar; un script ya aplicado no se reescribe, los cambios van en uno nuevo. Con esto el proyecto del volumen queda completo.
