# Cheat sheet · Capítulo 10 — Relaciones entre entidades

*Java Esencial · Volumen 2: Spring Boot · código del capítulo: tag `v2-cap10`*

## En una frase

Sabes relacionar entidades JPA con `@ManyToOne` y `@JoinColumn`, cargar las relaciones con `JOIN FETCH` para evitar el problema N+1 y exponer los préstamos por REST con un DTO en lugar de la entidad.

## Lo esencial

| Elemento | Para qué sirve |
|---|---|
| `@ManyToOne` | Muchos registros de esta tabla apuntan a uno de otra: muchos `Prestamo` para un `Usuario` (o un `Libro`) |
| `@JoinColumn(name = "usuario_id", nullable = false)` | Nombre de la columna de clave foránea en la tabla `prestamo`; sin ella Hibernate genera un nombre automático |
| `FetchType.LAZY` | Carga la relación solo cuando llamas al getter. Es lo recomendado |
| `FetchType.EAGER` | Carga la relación siempre con un JOIN automático. Es el valor por defecto de `@ManyToOne`: por eso escribes `LAZY` a mano |
| Problema N+1 | Cargas N préstamos y accedes a sus relaciones LAZY en un bucle: 2N+1 consultas en vez de una |
| `JOIN FETCH` | Instrucción JPQL que trae las entidades relacionadas en el mismo SELECT |
| `@Query` | Escribe la consulta JPQL en el propio método del repositorio |
| DTO (`PrestamoRespuesta`) | Aplana los datos de la respuesta y aísla el modelo interno; evita la `LazyInitializationException` |
| `record` como DTO | Inmutable, con getters automáticos; Jackson lo serializa sin configuración |
| `@Transactional` | Todas las operaciones del método se completan o ninguna (rollback automático); a nivel de clase cubre todos los métodos |
| `@Transactional(readOnly = true)` | Marca los métodos de solo lectura y permite optimizaciones a Hibernate |
| `@PatchMapping` | Verbo PATCH para actualizar solo una parte del recurso (la `fechaDevolucion`) |
| `@Column(nullable, unique, length, name)` | Restricciones de la columna en la base de datos; `nullable = false` complementa a `@NotNull`, no lo sustituye |
| `fechaDevolucion` a `null` | Así se representa un préstamo activo |

## Código mínimo

Las dos relaciones de `Prestamo`, siempre con `LAZY` explícito:

```java
@Entity
@Table(name = "prestamo")
public class Prestamo {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;   // null = préstamo activo
}
```

La consulta de `PrestamoJpaRepository` que trae usuario y libro en un solo SELECT:

```java
@Query("""
        SELECT p FROM Prestamo p JOIN FETCH p.usuario JOIN FETCH p.libro
        WHERE p.fechaDevolucion IS NULL
        """)
List<Prestamo> buscarActivosConUsuarioYLibro();
```

El controlador devuelve el DTO, nunca la entidad:

```java
@GetMapping
public List<PrestamoRespuesta> listarTodos() {
    return service.obtenerTodos().stream().map(PrestamoRespuesta::desde).toList();
}
```

## Comandos y peticiones

```http
### Listar todos los préstamos activos
GET http://localhost:8080/prestamos

### Préstamos activos del usuario con id 1
GET http://localhost:8080/prestamos/usuario/1

### Registrar nuevo préstamo: usuario 2 pide el libro 3
POST http://localhost:8080/prestamos/usuario/2/libro/3

### Registrar devolución del préstamo con id 1
PATCH http://localhost:8080/prestamos/1/devolucion
```

## Errores típicos

- **`LazyInitializationException ... no Session` al devolver `List<Prestamo>`** → la sesión JPA ya está cerrada cuando Jackson llama a `getUsuario()`. Devuelve `PrestamoRespuesta` con `.map(PrestamoRespuesta::desde)`.
- **Un fallo a mitad de una operación deja los datos a medias, sin rollback** → falta `@Transactional` en el servicio. Sin ella, cada operación del repositorio va en su propia transacción.
- **Los logs muestran JOINs con `libro` y `usuario` que no has pedido** → `@ManyToOne` sin `fetch` es `EAGER`. Pon `fetch = FetchType.LAZY` y usa `JOIN FETCH` solo donde haga falta.
- **`Referential integrity constraint violation` al arrancar** → `data.sql` referencia ids que no existen. Inserta primero libros y usuarios, después préstamos, y usa los ids que genera `IDENTITY` (1, 2, 3...).
- **`405 Method Not Allowed` con `Allow: PATCH`** → has llamado a la devolución con PUT. El endpoint es `PATCH /prestamos/{id}/devolucion`.

## En el Proyecto Biblioteca

Llegan las entidades `Usuario` y `Prestamo`, sus repositorios (`UsuarioJpaRepository`, `PrestamoJpaRepository`), `PrestamoService`, `PrestamoController` y el DTO `PrestamoRespuesta`. `ManejadorGlobalErrores` traduce ahora tres «no encontrado» a 404 (libro, usuario y préstamo, con `UsuarioNoEncontradoException` y `PrestamoNoEncontradoException` nuevas) y `data.sql` añade tres usuarios y tres préstamos: dos activos y uno devuelto.
