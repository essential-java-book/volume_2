# Práctica 15.2 — Imagen Docker optimizada con .dockerignore

## Enunciado

Sin un archivo `.dockerignore`, Docker envía todo el directorio del proyecto al daemon en
cada `docker build`, incluyendo `target/`, `.git/`, el IDE, etc. Esto ralentiza la
construcción innecesariamente.

1. Crea el archivo `.dockerignore` en la raíz del proyecto con las siguientes
   exclusiones: `.git/`, `.idea/`, `*.iml`, `target/classes/`, `target/test-classes/`,
   `target/surefire-reports/`.
2. Mide el tiempo de `docker build` antes y después de añadir `.dockerignore` con
   `time docker build -t biblioteca-api:test .`.
3. Añade una instrucción `LABEL` al Dockerfile con los metadatos de la imagen:
   `maintainer`, `version` y `description`.
4. Verifica los labels con `docker inspect biblioteca-api:test | grep -A 10 Labels`.

## Solución

### 1. `.dockerignore`, en la raíz del proyecto (junto al `Dockerfile`)

```gitignore
.git/
.idea/
*.iml
target/classes/
target/test-classes/
target/surefire-reports/
```

Estas rutas son relativas a la raíz del contexto de build (el directorio desde el que se
ejecuta `docker build .`, el mismo que contiene el `Dockerfile`) — coinciden con las
carpetas que Maven genera localmente al compilar/testear fuera de Docker
(`target/classes`, `target/test-classes`, `target/surefire-reports`) y que un desarrollador
suele tener ya en su máquina de trabajo, aunque el propio `Dockerfile` nunca las use: la
primera etapa del build (`FROM maven:3.9-eclipse-temurin-21 AS build`) compila el proyecto
*dentro* del contenedor a partir del código fuente (`COPY src/ src/`), no reutiliza nada de
un `target/` que ya exista en el host.

> **Nota:** por eso el `.dockerignore` no necesita excluir `target/` entero —solo importa
> para el *tamaño del contexto* que se envía al daemon de Docker, no para la corrección
> del build: aunque `target/*.jar` (el artefacto final que sí genera un build local)
> llegara a enviarse, el `Dockerfile` no lo copia en ningún `COPY`, así que no tendría
> ningún efecto sobre la imagen resultante. La ganancia de `.dockerignore` es puramente de
> velocidad de transferencia del contexto, no de corrección.

### 2. Medir el tiempo de `docker build`

```bash
# Antes de añadir .dockerignore (con un target/ local ya compilado)
time docker build -t biblioteca-api:test .
```
```
real    0m47.312s
user    0m0.089s
sys     0m0.112s
```

```bash
# Después de añadir .dockerignore
time docker build -t biblioteca-api:test .
```
```
real    0m31.548s
user    0m0.076s
sys     0m0.098s
```

> **Nota:** estos tiempos son ilustrativos —dependen del tamaño real de `target/` en el
> momento de medir, de la caché de capas de Docker y de la máquina—, y no se han ejecutado
> en este entorno (sin acceso a un daemon Docker). Lo relevante no es la cifra exacta, sino
> dónde se concentra la diferencia: el tiempo `real` baja principalmente en el paso
> *"Sending build context to Docker daemon"*, que Docker imprime al principio del build,
> antes de ejecutar ninguna instrucción del `Dockerfile` — ese paso es proporcional al
> tamaño de lo que `.dockerignore` deja fuera, no al tiempo de compilación de Maven (que no
> cambia entre las dos ejecuciones).

### 3. `LABEL` en el `Dockerfile`

```dockerfile
# Etapa 3: imagen final, usuario sin privilegios
FROM eclipse-temurin:21-jre

LABEL maintainer="Basilio Fajardo Gálvez <basilio.fajardo.galvez@gmail.com>"
LABEL version="2.0"
LABEL description="API REST de la Biblioteca Municipal El Quijote (Java Esencial, Volumen 2)"

RUN groupadd --system biblioteca \
    && useradd --system --gid biblioteca biblioteca
WORKDIR /app
COPY --from=layers /build/dependencies/ ./
COPY --from=layers /build/spring-boot-loader/ ./
COPY --from=layers /build/snapshot-dependencies/ ./
COPY --from=layers /build/application/ ./
USER biblioteca

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

Los `LABEL` se colocan justo después del `FROM` de la **última** etapa (la que produce la
imagen final): un `Dockerfile` multietapa como este descarta las imágenes intermedias
(`build`, `layers`) una vez terminan su trabajo, así que unos metadatos puestos en esas
etapas no sobrevivirían en `biblioteca-api:test` — solo los de la etapa final quedan
grabados en la imagen que se etiqueta y se publica. `version="2.0"` coincide, a propósito,
con `biblioteca.version=2.0` que ya usa `application-dev.properties`/`application-prod.properties`
—un único número de versión, no dos que puedan desincronizarse.

### 4. Comprobación con `docker inspect`

```bash
docker inspect biblioteca-api:test | grep -A 10 Labels
```
```json
"Labels": {
    "maintainer": "Basilio Fajardo Gálvez <basilio.fajardo.galvez@gmail.com>",
    "version": "2.0",
    "description": "API REST de la Biblioteca Municipal El Quijote (Java Esencial, Volumen 2)"
},
```

Los tres pares clave-valor aparecen dentro del objeto `Labels` de `docker inspect`, uno por
cada instrucción `LABEL` del `Dockerfile` — Docker acumula todos los `LABEL` de la etapa
final en ese mismo objeto, en el orden en que aparecen en el fichero, sin que haga falta
combinarlos en una sola instrucción `LABEL clave1=valor1 clave2=valor2 ...` (aunque esa
forma abreviada también sería válida y produciría el mismo resultado).
