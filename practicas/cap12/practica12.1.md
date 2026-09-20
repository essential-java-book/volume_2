# Práctica 12.1 — Migración V3: índice de búsqueda

## Enunciado

Crea el script `V3__indice_busqueda_libro.sql` que añada un índice en la columna
`titulo` de la tabla `libro` para acelerar las búsquedas por texto. Verifica en la consola
H2 que `flyway_schema_history` contiene las tres versiones aplicadas.

## Solución

`src/main/resources/db/migration/V3__indice_busqueda_libro.sql`:

```sql
-- V3: índice en libro.titulo para acelerar las búsquedas por texto
-- (Capítulo 11: findByTituloIgnoreCase, buscarPorTextoPaginado).

CREATE INDEX idx_libro_titulo ON libro(titulo);
```

El nombre del fichero sigue exactamente la convención que ya establecen `V1` y `V2`:
prefijo `V` + número de versión + doble guión bajo + una descripción en minúsculas
separada por guiones bajos. Flyway detecta el fichero nuevo en
`src/main/resources/db/migration/` sin ninguna configuración adicional —
`spring.flyway.enabled=true` (ya presente en `application-dev.properties` desde el cuerpo
del capítulo) hace que Spring Boot ejecute Flyway automáticamente al arrancar, antes de que
Hibernate valide el esquema.

### Por qué basta con un `CREATE INDEX`, sin `ALTER TABLE`

A diferencia de `V2__aniadir_isbn_a_libro.sql` (que sí necesita `ALTER TABLE libro ADD
COLUMN ...` porque cambia la estructura de la tabla), un índice no es una columna ni
modifica los datos existentes: es una estructura auxiliar que la base de datos mantiene
por su cuenta para acelerar las consultas que filtran u ordenan por esa columna. `CREATE
INDEX` es una sentencia completa por sí misma, igual que ya usa `V1` para
`idx_prestamo_usuario`/`idx_prestamo_libro`.

### Comprobación en la consola H2

Con la aplicación arrancada (perfil `dev`, `spring.h2.console.enabled=true`), en
`http://localhost:8080/h2-console` conectando a `jdbc:h2:mem:biblioteca`:

```sql
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;
```

```
VERSION | DESCRIPTION              | SUCCESS
1       | crear esquema            | TRUE
2       | aniadir isbn a libro     | TRUE
3       | indice busqueda libro    | TRUE
```

Las tres filas con `SUCCESS = TRUE` confirman que Flyway aplicó `V3` en orden, después de
`V1` y `V2`, sin reejecutar las que ya estaban aplicadas — el comportamiento normal de
Flyway en cada arranque: compara las migraciones del classpath contra
`flyway_schema_history` y solo ejecuta las que faltan.
