# Práctica 12.3 — Script de datos iniciales versionado

## Enunciado

Actualmente los datos iniciales van en `data.sql`, que Flyway no gestiona. Crea el script
`V1.1__datos_iniciales.sql` con los mismos INSERT de libros, usuarios y préstamos que hay
en `data.sql`. Elimina `data.sql` y verifica que al arrancar la aplicación los datos
siguen cargándose (ahora gestionados por Flyway).

## Solución

`src/main/resources/db/migration/V1.1__datos_iniciales.sql`, con los mismos `INSERT` que
tenía `data.sql`:

```sql
-- V1.1: datos iniciales de desarrollo, migrados desde data.sql para
-- que Flyway los gestione y queden registrados en
-- flyway_schema_history como el resto del esquema.

INSERT INTO libro (titulo, autor, anio, isbn) VALUES ('El Quijote', 'Miguel de Cervantes', 1605, '978-84-376-0494-7');
INSERT INTO libro (titulo, autor, anio, isbn) VALUES ('1984', 'George Orwell', 1949, '978-84-663-2674-7');
INSERT INTO libro (titulo, autor, anio, isbn) VALUES ('Dune', 'Frank Herbert', 1965, '978-84-450-7715-6');

INSERT INTO usuario (nombre, email) VALUES ('Ana García', 'ana@bib.es');
INSERT INTO usuario (nombre, email) VALUES ('Pedro López', 'pedro@bib.es');
INSERT INTO usuario (nombre, email) VALUES ('Laura Martín', 'laura@bib.es');

INSERT INTO prestamo (usuario_id, libro_id, fecha_prestamo, fecha_devolucion) VALUES (1, 1, '2026-08-01', NULL);
INSERT INTO prestamo (usuario_id, libro_id, fecha_prestamo, fecha_devolucion) VALUES (2, 2, '2026-08-05', '2026-08-20');
INSERT INTO prestamo (usuario_id, libro_id, fecha_prestamo, fecha_devolucion) VALUES (3, 3, '2026-08-10', NULL);
```

Se elimina `src/main/resources/data.sql`, y en `application-dev.properties` ya no hace
falta `spring.sql.init.mode=always` (esa propiedad controla el mecanismo de
inicialización de datos de Spring Boot, independiente de Flyway y pensado justamente para
ficheros como `data.sql`/`schema.sql`; sin `data.sql`, no tiene nada que inicializar):

```properties
# spring.sql.init.mode=always   -- eliminado: ya no hay data.sql, los datos
#                                  iniciales los carga Flyway con V1.1
```

### Por qué el número de versión es `V1.1`, no `V5`

Flyway numera las versiones por orden, pero **no** exige que sean enteros consecutivos:
`V1.1` es una versión válida que Flyway ordena *entre* `V1` y `V2` (compara los números
componente a componente: `1` == `1`, luego `(nada)` < `1`, así que `V1` < `V1.1` < `V2`).
Eso es exactamente lo que pide el enunciado — tratar los datos iniciales como parte del
esquema original, aplicados justo después de crear las tablas (`V1`) pero antes de
añadir el ISBN (`V2`) — en vez de como una migración más reciente con `V5`, que los
colocaría (semánticamente, aunque no en el tiempo real de ejecución) después del índice y
la tabla de categorías.

> **Nota:** en la práctica, como `V1.1` nunca se ha aplicado todavía en la base de datos
> de desarrollo (que hasta ahora usaba `data.sql`, no Flyway, para estos datos), Flyway la
> ejecutará junto con `V3`/`V4` si ya existían, respetando el orden `V1` → `V1.1` → `V2` →
> `V3` → `V4` la primera vez que arranque tras este cambio — el orden de *ejecución* sigue
> las reglas de orden anteriores, no el momento en que se creó el fichero.

### Comprobación

Tras eliminar `data.sql` y arrancar la aplicación:

```sql
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;
```

```
VERSION | DESCRIPTION              | SUCCESS
1       | crear esquema            | TRUE
1.1     | datos iniciales          | TRUE
2       | aniadir isbn a libro     | TRUE
3       | indice busqueda libro    | TRUE
4       | crear tabla categoria    | TRUE
```

```terminal
GET /libros
```

Sigue devolviendo los tres libros de siempre (El Quijote, 1984, Dune) — los mismos datos
que antes cargaba `data.sql`, ahora aplicados por Flyway como parte de `V1.1` y, por
tanto, con historial y checksum propios en `flyway_schema_history`, igual que el resto del
esquema.
