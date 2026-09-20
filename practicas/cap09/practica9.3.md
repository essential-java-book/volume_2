# Práctica 9.3 — Datos iniciales ampliados

## Enunciado

Amplía el fichero `data.sql` con cinco libros adicionales de tu elección (título, autor y
año correctos). Arranca la aplicación y verifica en la consola H2 que la tabla `LIBRO`
contiene los ocho registros.

## Solución

`data.sql`, tal como queda al terminar el cuerpo del capítulo (tres libros):

```sql
INSERT INTO libro (titulo, autor, anio) VALUES ('El Quijote', 'Miguel de Cervantes', 1605);
INSERT INTO libro (titulo, autor, anio) VALUES ('1984', 'George Orwell', 1949);
INSERT INTO libro (titulo, autor, anio) VALUES ('Dune', 'Frank Herbert', 1965);
```

Con cinco libros más, históricamente correctos (título, autor y año reales):

```sql
INSERT INTO libro (titulo, autor, anio) VALUES ('El Quijote', 'Miguel de Cervantes', 1605);
INSERT INTO libro (titulo, autor, anio) VALUES ('1984', 'George Orwell', 1949);
INSERT INTO libro (titulo, autor, anio) VALUES ('Dune', 'Frank Herbert', 1965);
INSERT INTO libro (titulo, autor, anio) VALUES ('Cien años de soledad', 'Gabriel García Márquez', 1967);
INSERT INTO libro (titulo, autor, anio) VALUES ('Fahrenheit 451', 'Ray Bradbury', 1953);
INSERT INTO libro (titulo, autor, anio) VALUES ('Crónica de una muerte anunciada', 'Gabriel García Márquez', 1981);
INSERT INTO libro (titulo, autor, anio) VALUES ('El nombre de la rosa', 'Umberto Eco', 1980);
INSERT INTO libro (titulo, autor, anio) VALUES ('Rebelión en la granja', 'George Orwell', 1945);
```

Ocho filas en total: las tres originales, sin tocar, más las cinco nuevas.

### Verificación en la consola H2

Con `spring.h2.console.enabled=true` (ya presente en `application-dev.properties`), la
consola está disponible en `http://localhost:8080/h2-console` mientras la aplicación está
arrancada con el perfil `dev`. Los datos de conexión son los mismos que
`spring.datasource.*` de `application.properties`: JDBC URL `jdbc:h2:mem:biblioteca`,
usuario `sa`, contraseña en blanco.

Ejecutando `SELECT * FROM LIBRO;` en la consola, la tabla muestra las ocho filas, con
`ID` autoincremental del 1 al 8 (en el mismo orden en que aparecen los `INSERT` de
`data.sql`, porque `spring.jpa.defer-datasource-initialization=true` hace que Hibernate
cree primero el esquema y `data.sql` se ejecute justo después, en un único arranque).

### Por qué el orden de los `INSERT` importa aquí

`spring.jpa.hibernate.ddl-auto=create-drop` recrea el esquema entero (incluida la tabla
`libro`, vacía) en cada arranque de la aplicación; `data.sql` es lo que la rellena de
nuevo cada vez. Esto significa que los ocho libros de esta práctica **no persisten** entre
un reinicio y otro de forma acumulativa — desaparecen y se vuelven a crear desde cero cada
vez que arranca la aplicación, siempre los mismos ocho, nunca dieciséis tras dos arranques.
Este comportamiento es intencionado en este capítulo (una base de datos en memoria, pensada
para desarrollo) y cambiará en el Capítulo 12, cuando `ddl-auto=validate` con Flyway deje
de recrear el esquema en cada arranque.
