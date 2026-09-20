# Práctica 13.1 — Repasar las reglas por rol

## Enunciado

> **Nota:** el enunciado de esta práctica se corrigió durante la revisión de la
> colección — en el manuscrito original pedía *implementar* las reglas por rol como si
> fueran un ejercicio pendiente, pero en el repositorio real (y en el cuerpo corregido del
> capítulo, sección 13.4) `SeguridadConfig` ya las incluye. El enunciado vigente es:

Las reglas por rol de `SecurityFilterChain` (`hasRole("ADMIN")` en `POST`/`PUT`/`DELETE`
de `/libros`, `hasAnyRole("ADMIN", "USER")` en `/prestamos/**`) ya están integradas en
`SeguridadConfig` (sección 13.4). Como repaso, prueba con `pruebas.http` los dos roles: un
usuario `ADMIN` debe poder crear/editar/borrar libros; un usuario `USER` debe recibir 403
Forbidden al intentarlo, y 200 OK en `GET`.

## Solución

Las reglas ya presentes en `SeguridadConfig` (sección 13.4 del capítulo, sin cambios en
esta práctica):

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.GET, "/libros", "/libros/**").permitAll()
        .requestMatchers("/docs/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
        .requestMatchers(HttpMethod.POST, "/libros").hasRole("ADMIN")
        .requestMatchers(HttpMethod.PUT, "/libros/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/libros/**").hasRole("ADMIN")
        .requestMatchers("/prestamos/**").hasAnyRole("ADMIN", "USER")
        .anyRequest().authenticated())
```

Credenciales de `data.sql` para el repaso: `admin` (rol `ADMIN`, contraseña `admin123`) y
`bibliotecario` (rol `USER`, contraseña `user123`) — ambas con la contraseña ya hasheada
con BCrypt en la base de datos, tal como carga `BibliotecaUserDetailsService`.

Peticiones de repaso (formato `pruebas.http`, con autenticación HTTP Basic):

```http
### GET público, sin credenciales
GET http://localhost:8080/libros

### ADMIN puede crear
POST http://localhost:8080/libros
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json

{ "titulo": "Fahrenheit 451", "autor": "Ray Bradbury", "anio": 1953 }

### USER no puede crear -> 403
POST http://localhost:8080/libros
Authorization: Basic YmlibGlvdGVjYXJpbzp1c2VyMTIz
Content-Type: application/json

{ "titulo": "Fahrenheit 451", "autor": "Ray Bradbury", "anio": 1953 }

### USER sí puede listar -> 200
GET http://localhost:8080/libros
Authorization: Basic YmlibGlvdGVjYXJpbzp1c2VyMTIz
```

(`YWRtaW46YWRtaW4xMjM=` es `admin:admin123` y `YmlibGlvdGVjYXJpbzp1c2VyMTIz` es
`bibliotecario:user123`, ambos en Base64 — así codifica HTTP Basic las credenciales en la
cabecera `Authorization`.)

### Resultados esperados

| Petición | Usuario | Resultado |
|---|---|---|
| `GET /libros` | sin credenciales | 200 OK (ruta pública) |
| `POST /libros` | `admin` (ADMIN) | 201 Created |
| `POST /libros` | `bibliotecario` (USER) | 403 Forbidden |
| `GET /libros` | `bibliotecario` (USER) | 200 OK |

El 403 (no 401) en la tercera fila es la distinción clave: `bibliotecario` **sí** se
autentica correctamente (sus credenciales son válidas), pero su rol `USER` no está entre
los permitidos por `hasRole("ADMIN")` en esa ruta — 401 significaría "no sé quién eres",
403 significa "sé quién eres, y no tienes permiso para esto".
