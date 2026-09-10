# Java Esencial · Volumen 2 — Spring Boot: De la consola a una API REST profesional

Material complementario del libro **«Java Esencial: De Principiante a
Experto. Volumen 2: Spring Boot — De la consola a una API REST
profesional»**, de Basilio Fajardo Gálvez. Segundo repositorio de la
colección (`volume_1` … `volume_6`), misma estructura que el resto.

Aquí encontrarás el **Proyecto Biblioteca** reescrito en Spring Boot
(la misma *Biblioteca Municipal «El Quijote»* del Volumen 1, ahora
como API REST) tal como queda al terminar cada capítulo, más las
prácticas, los tests de repaso y las chuletas de cada capítulo.

## Estructura

| Carpeta | Contenido |
|---|---|
| `proyecto/` | El Proyecto Biblioteca (Maven, Spring Boot). Un tag por capítulo (`v2-cap01` … `v2-cap15`); `v2.0.0` es el estado final del volumen. |
| `practicas/` | Solución de cada práctica del libro, en `capNN/practicaN.M.md`. Pendiente (se añade al cerrar el volumen). |
| `quizzes/` | Tests de conocimientos interactivos por capítulo. Pendiente. |
| `cheatsheets/` | Chuletas de una página por capítulo. Pendiente. |
| `tools/` | Scripts para verificar el proyecto capítulo a capítulo (Linux/macOS: `.sh`; Windows: `.ps1`). |

## El proyecto capítulo a capítulo

Cada capítulo del libro termina con una sección «Proyecto Biblioteca
— Capítulo N». El código de esa sección es el que hay en el tag
`v2-capNN`:

```bash
git clone https://github.com/essential-java-book/volume_2.git
cd volume_2
git checkout v2-cap09     # el proyecto tal como queda al terminar el capítulo 9
git checkout master       # volver al estado final
```

Si no usas Git, en la página del repositorio elige el tag en el
desplegable de ramas (**main ▾ → Tags**) y descarga el ZIP de ese
capítulo.

| Cap. | Tag | Lo que añade al proyecto |
|---|---|---|
| 1 | `v2-cap01` | Sin código -- revisión del estado final del Volumen 1 |
| 2 | `v2-cap02` | Proyecto Spring Boot nuevo (Initializr); `HolaMundoController` |
| 3 | `v2-cap03` | Paquetes `dominio/repositorio/servicio/controlador`; IoC |
| 4 | `v2-cap04` | Configuración y perfiles (`dev`/`prod`), `BibliotecaConfig` |
| 5 | `v2-cap05` | `LibroController` con CRUD completo (en memoria) |
| 6 | `v2-cap06` | Bean Validation y `ManejadorGlobalErrores` (`ProblemDetail`) |
| 7 | `v2-cap07` | Documentación OpenAPI/Swagger UI en `/docs` |
| 8 | `v2-cap08` | Tests de la capa REST con MockMvc (`LibroControllerTest`) |
| 9 | `v2-cap09` | Spring Data JPA, H2 en memoria, `data.sql` |
| 10 | `v2-cap10` | `Usuario`, `Prestamo`, relaciones `@ManyToOne`, `PrestamoController` |
| 11 | `v2-cap11` | Paginación, orden y búsqueda (`pagina/tamanio/orden/direccion/buscar`) |
| 12 | `v2-cap12` | Migraciones Flyway (`V1`, `V2`); PostgreSQL en `prod` |
| 13 | `v2-cap13` | Autenticación con Spring Security (HTTP Basic, roles) |
| 14 | `v2-cap14` | Autenticación sin estado con JWT (`/auth/login`) |
| 15 | `v2-cap15` | Actuator, Docker multicapa, `docker-compose.yml` · `v2.0.0` |

## Cómo compilar y ejecutar

Requisitos: JDK 21 (Temurin recomendado) y Maven 3.9+. Desde el
capítulo 15, Docker y Docker Compose para el perfil `prod`.

```bash
cd proyecto
mvn spring-boot:run                    # perfil dev, H2 en memoria
mvn clean package && mvn test          # compilar y ejecutar tests

docker compose up --build              # cap. 15: perfil prod, PostgreSQL
```

Ver `proyecto/README.md` para el detalle de arranque y autenticación,
y `proyecto/pruebas.http` para peticiones de ejemplo por capítulo.

### Scripts de `tools/`

| Script | Qué hace |
|---|---|
| `verificar-todo.sh` / `verificar-todo.ps1` | Recorre los 15 tags: compila cada uno con Maven y, desde el capítulo 8, ejecuta también sus tests. |

En Windows: `powershell -ExecutionPolicy Bypass -File tools\verificar-todo.ps1` desde la raíz del repositorio, con `git`, `mvn` y `java` en el PATH.

## Reglas del proyecto (para que tu código coincida con el del libro)

- Paquete base `com.javaesencial.biblioteca`, subpaquetes `dominio`, `repositorio`, `servicio`, `controlador`, `configuracion`, `seguridad` -- nunca `modelo`.
- Sin prefijo `/api`: los endpoints son `/libros`, `/prestamos`, `/auth/**`, `/actuator/**`.
- Swagger UI en `/docs` (no en la ruta por defecto `/swagger-ui.html`).
- Parámetros de paginación en español: `pagina`, `tamanio`, `orden`, `direccion`, `buscar` (nunca `page`/`size`/`sort`).
- Los scripts Flyway ya aplicados (`V1`–`V3`) son inmutables: un cambio de esquema siempre es un `V4__…` nuevo, nunca una edición de los anteriores.
- Ninguna línea de código supera los 66 caracteres.
- Código en español (identificadores sin tildes ni ñ), comentarios en español de España; marcadores `[OK]`/`[XX]`/`[!]` donde el capítulo muestre salida de consola o terminal.

## La colección

| Vol. | Título | Repositorio |
|---|---|---|
| 1 | Fundamentos del Lenguaje | `volume_1` |
| 2 | Spring Boot — De la consola a una API REST profesional | `volume_2` (este) |
| 3 | Microservicios — Del monolito a un sistema distribuido en producción | `volume_3` |
| 4 | Java de Alta Demanda | `volume_4` |
| 5 | Arquitectura Empresarial — DDD, Kafka, Seguridad y Java Moderno | `volume_5` |
| 6 | Java Inteligente | `volume_6` |

¿Has encontrado una errata o un error en el código? Abre un *issue*
en este repositorio.
