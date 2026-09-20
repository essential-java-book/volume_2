# Cheat sheet · Capítulo 9 — Spring Data JPA y base de datos

*Java Esencial · Volumen 2: Spring Boot · código del capítulo: tag `v2-cap09`*

## En una frase

Sabes sustituir el repositorio en memoria por una base de datos real: conviertes `Libro` en entidad JPA, obtienes el CRUD extendiendo `JpaRepository` y trabajas con H2 en desarrollo dejando PostgreSQL preparado para producción.

## Lo esencial

| Elemento | Para qué sirve |
|---|---|
| JPA / Hibernate / Spring Data JPA | Especificación (anotaciones e interfaces) / implementación que ejecuta el SQL / capa que genera los repositorios |
| `spring-boot-starter-data-jpa` | Starter con Hibernate, Spring Data JPA y el pool HikariCP |
| `com.h2database:h2` (`scope` `runtime`) | Base de datos H2 en memoria para desarrollo y tests |
| `@Entity` + `@Table(name = "libro")` | Marca la clase como entidad y fija el nombre de la tabla |
| `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)` | Clave primaria con autoincremento delegado en la base de datos |
| `@Column(length = 200)` | Longitud de la columna `VARCHAR`, a juego con el `@Size` del campo |
| Requisitos de una entidad | Constructor vacío, getters y setters, `id` de tipo `Long` (no `long`) y clase no `final` |
| `JpaRepository<Libro, Long>` | Interfaz con el CRUD completo: `findAll`, `findById`, `save`, `deleteById`, `existsById`, `count` |
| `save(libro)` | Con `id` nulo hace INSERT; con `id`, UPDATE |
| Query Derivation | Consultas a partir del nombre del método: `findByAutor`, `findByAnioGreaterThan`, `findByTituloContainingIgnoreCase` |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` en desarrollo; `update` en producción hasta que llegue Flyway, y entonces `validate` |
| `data.sql` | Inserta los datos iniciales al arrancar |
| `spring.jpa.defer-datasource-initialization=true` | Hace que `data.sql` se ejecute después de que Hibernate cree las tablas |
| `spring.h2.console.enabled=true` | Activa la consola web de H2 en `/h2-console` (solo en el perfil `dev`) |

## Código mínimo

La entidad `Libro` (recortada):

```java
@Entity
@Table(name = "libro")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 1, max = 200, message = "El título no puede superar los 200 caracteres")
    @Column(length = 200)
    private String titulo;

    public Libro() {
    }
}
```

El repositorio: basta con declarar la interfaz.

```java
package com.javaesencial.biblioteca.repositorio;

import com.javaesencial.biblioteca.dominio.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroJpaRepository extends JpaRepository<Libro, Long> {
}
```

H2 y JPA en `application-dev.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:biblioteca
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.defer-datasource-initialization=true
```

## Comandos y peticiones

```sql
-- Consola H2: http://localhost:8080/h2-console
-- JDBC URL: jdbc:h2:mem:biblioteca · User Name: sa · Password: (vacío)

SELECT * FROM LIBRO;
SELECT * FROM LIBRO WHERE ANIO > 2000;
SHOW COLUMNS FROM LIBRO;
```

## Errores típicos

- **`InstantiationException: No default constructor for entity`** → Hibernate crea las instancias por reflexión; añade siempre `public Libro() {}`.
- **`Table "LIBRO" not found` al ejecutar `data.sql`** → el script se lanza antes de que Hibernate cree las tablas; añade `spring.jpa.defer-datasource-initialization=true`.
- **Los datos desaparecen tras reiniciar en producción** → `create-drop` es solo para desarrollo; en `application-prod.properties` usa `spring.jpa.hibernate.ddl-auto=update` (y `validate` cuando Flyway gestione el esquema).
- **`save()` hace UPDATE en lugar de INSERT con un libro nuevo** → el `id` es `long` primitivo y vale 0, nunca `null`; decláralo como `Long`.
- **La consola H2 aparece vacía o sin la tabla `LIBRO`** → te has conectado a la URL por defecto del formulario (`jdbc:h2:~/test`); escribe exactamente `jdbc:h2:mem:biblioteca`.

## En el Proyecto Biblioteca

`Libro` pasa a ser entidad JPA, se crea `LibroJpaRepository` y `BibliotecaService` lo usa en lugar de `RepositorioLibros`, que se elimina junto con `CargadorDatosPrueba`. Los tres libros iniciales se cargan ahora desde `data.sql`. `application-dev.properties` configura H2 con `create-drop` y la consola web; `application-prod.properties` queda preparado para PostgreSQL con `ddl-auto=update`, y el `pom.xml` incorpora `spring-boot-starter-data-jpa`, `h2` y el driver `postgresql`. El controlador y sus tests no cambian.
