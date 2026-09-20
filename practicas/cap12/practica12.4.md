# Práctica 12.4 — Reparar un checksum incorrecto

## Enunciado

Para entender el mecanismo de protección de Flyway: haz una copia de seguridad de
`V1__crear_esquema.sql`, añade un comentario al fichero y arrancar la aplicación para ver
el error de checksum. Luego usa el comando de reparación de Flyway para actualizar el
historial y comprueba el resultado en `flyway_schema_history`.

```properties
# Añadir temporalmente en application-dev.properties para reparar
spring.flyway.repair-on-migrate=true
```

## Solución

### 1. Provocar el error de checksum

Copia de seguridad, y comentario añadido al final de `V1__crear_esquema.sql`:

```sql
-- V1: esquema inicial (libro, usuario, prestamo).
-- ... (resto del fichero sin cambios) ...

CREATE INDEX idx_prestamo_usuario ON prestamo(usuario_id);
CREATE INDEX idx_prestamo_libro ON prestamo(libro_id);

-- comentario añadido para la Práctica 12.4: provoca un checksum distinto
```

Al arrancar la aplicación (con `V1` ya aplicada anteriormente, registrada en
`flyway_schema_history` con su checksum original), Flyway detecta que el contenido del
fichero `V1` ya no coincide con el checksum guardado y **detiene el arranque**:

```terminal
Migration checksum mismatch for migration version 1
-> Applied to database : -123456789
-> Resolved locally    : 987654321
Either revert the changes to the migration, or run repair to update the schema history.
```

La aplicación no llega a arrancar: Spring Boot falla al inicializar el contexto porque el
`FlywayMigrationInitializer` (que se ejecuta antes que Hibernate) lanza
`FlywayValidateException`.

### 2. Por qué Flyway reacciona así

Este es exactamente el mecanismo de protección que explica el cuerpo del capítulo:
`flyway_schema_history` no solo registra *qué* versiones se han aplicado, sino un
checksum (CRC32) del contenido de cada script en el momento de aplicarlo. Si alguien
modifica después un script ya ejecutado —por accidente, o a propósito pensando que "solo
es un comentario"—, Flyway ya no puede garantizar que la base de datos actual sea el
resultado de ejecutar exactamente ese fichero: podría haber cambiado cualquier cosa, no
solo el comentario. Por eso falla de forma ruidosa en vez de ignorar la diferencia — la
alternativa (permitir migraciones ya aplicadas modificarse en silencio) rompería la
garantía central de Flyway: que el historial describe con exactitud cómo se llegó al
esquema actual.

### 3. Reparar con `flyway:repair`

En vez de la propiedad `spring.flyway.repair-on-migrate=true` sugerida en el enunciado
—que no existe como tal en Flyway/Spring Boot; la reparación es una **operación
explícita**, no un modo automático que se activa por configuración y se ejecuta en cada
arranque—, la vía real es el plugin de Maven. El `pom.xml` de este proyecto solo trae la
dependencia `flyway-core` (la librería que usa Spring Boot para migrar automáticamente al
arrancar); el plugin de Maven que expone comandos como `flyway:repair` desde la línea de
comandos, fuera del ciclo de vida de arranque de la aplicación, no está configurado
todavía y hay que añadirlo:

```xml
<plugin>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <configuration>
        <url>jdbc:h2:tcp://localhost/mem:biblioteca</url>
        <user>sa</user>
        <password></password>
    </configuration>
</plugin>
```

```bash
mvn flyway:repair
```

`flyway:repair` recalcula el checksum de cada script del classpath y actualiza
`flyway_schema_history` para que coincida con el contenido *actual* de los ficheros —no
deshace el cambio en el fichero ni modifica el esquema real de la base de datos, solo
pone de acuerdo el historial con lo que hay ahora en disco. Tras repararlo, un arranque
normal de la aplicación ya no encuentra ninguna discrepancia (el checksum guardado ahora
es el del fichero con el comentario añadido) y continúa con normalidad.

> **Nota:** una H2 en memoria (`jdbc:h2:mem:...`) solo vive mientras la conexión que la
> creó sigue abierta — para que el plugin de Maven, ejecutado en un proceso aparte, pueda
> ver la misma base de datos que la aplicación, hace falta arrancar H2 en modo servidor
> TCP (de ahí `jdbc:h2:tcp://localhost/mem:biblioteca` en vez de `jdbc:h2:mem:biblioteca`)
> o, más simple para probar esto manualmente, usar una URL de fichero
> (`jdbc:h2:file:./data/biblioteca`) en vez de en memoria mientras se hace esta práctica.
> En `prod`, contra PostgreSQL real, esta distinción no existe: la URL de
> `application-prod.properties` sirve tal cual para el plugin.

> **Nota:** en un proyecto real, la reacción correcta casi siempre es la contraria a
> reparar: **revertir el cambio** en el script ya aplicado (`git checkout` sobre
> `V1__crear_esquema.sql`, deshaciendo el comentario) y, si de verdad hace falta ese
> cambio, expresarlo como una migración *nueva* (`V5__...sql`). `repair` existe para
> casos legítimos —por ejemplo, un script que se reformateó sin cambiar su significado
> (espacios en blanco, saltos de línea) y cuyo checksum cambió por eso—, no como atajo
> habitual para editar migraciones ya aplicadas.

### Comprobación

```sql
SELECT version, description, checksum, success FROM flyway_schema_history WHERE version = '1';
```

Antes de `repair`, el arranque ni siquiera llega a ejecutar esta consulta con éxito (la
aplicación no arranca). Después de `mvn flyway:repair`:

```
VERSION | DESCRIPTION   | CHECKSUM   | SUCCESS
1       | crear esquema | 987654321  | TRUE
```

El `CHECKSUM` almacenado ahora coincide con el del fichero actual (con el comentario), y
un arranque normal de la aplicación ya no reporta ningún error de validación.
