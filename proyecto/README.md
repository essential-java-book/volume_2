# biblioteca-spring

API REST de la *Biblioteca Municipal «El Quijote»*, proyecto que
recorre el libro **Java Esencial: De Principiante a Experto.
Volumen 2 — Spring Boot: De la consola a una API REST profesional**,
de Basilio Fajardo Gálvez.

Continúa el Proyecto Biblioteca del Volumen 1 (`com.javaesencial:biblioteca:1.0.0`),
reescrito desde cero en Spring Boot a partir del capítulo 2 (Spring
Initializr). Ver `../README.md` para la tabla capítulo → tag y
`VOL 2/companion/ESPECIFICACION-CANONICA-vol2.md` para el estado de
partida heredado y la evolución completa capítulo a capítulo.

## Requisitos

- JDK 21 (Temurin recomendado)
- Maven 3.9+
- Docker y Docker Compose (solo para el capítulo 15 en adelante)

## Arrancar en desarrollo (perfil `dev`, H2 en memoria)

```bash
mvn spring-boot:run
```

La API queda en `http://localhost:8080`. Swagger UI (desde el
capítulo 7) en `http://localhost:8080/docs`.

## Compilar y ejecutar los tests

```bash
mvn clean package
mvn test
```

## Arrancar con Docker Compose (perfil `prod`, PostgreSQL)

Desde el capítulo 15:

```bash
docker compose up --build
```

Levanta `db` (PostgreSQL 16) y `api` (esta aplicación, perfil
`prod`, migraciones Flyway aplicadas automáticamente). La API
queda igualmente en `http://localhost:8080`.

## Autenticación

Desde el capítulo 14, la API usa JWT sin estado:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

El token devuelto se envía como `Authorization: Bearer <token>`
en las peticiones que lo requieran (ver `pruebas.http`).

Usuarios de `data.sql` (solo perfil `dev`): `admin`/`admin123`
(`ROLE_ADMIN`) y `bibliotecario`/`user123` (`ROLE_USER`).
