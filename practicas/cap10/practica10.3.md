# Práctica 10.3 — Historial completo de préstamos de un usuario

## Enunciado

Añade en `PrestamoJpaRepository` un método derivado que devuelva todos los préstamos de
un usuario (activos y devueltos), ordenados por `fechaPrestamo` de más reciente a más
antiguo. Añade el endpoint `GET /prestamos/usuario/{usuarioId}/historial` que lo use.

## Solución

`PrestamoJpaRepository` con el método derivado añadido:

```java
public interface PrestamoJpaRepository extends JpaRepository<Prestamo, Long> {

    // ... las consultas @Query del cuerpo del capítulo (y, si se ha hecho la
    // Práctica 10.2, existsByLibroIdAndFechaDevolucionIsNull) ...

    List<Prestamo> findByUsuarioIdOrderByFechaPrestamoDesc(Long usuarioId);
}
```

A diferencia de `buscarActivosPorUsuarioConLibro` (una consulta manual con `@Query`
porque necesita `JOIN FETCH` para traer `usuario` y `libro` en la misma consulta), este
método sí se puede expresar completo como método derivado: `findBy` + `UsuarioId`
(navega la asociación `usuario.id`) + `OrderByFechaPrestamoDesc` (ordena por el campo
`fechaPrestamo`, descendente — de más reciente a más antiguo). **No** lleva ninguna
condición sobre `fechaDevolucion`: a diferencia de las tres consultas del cuerpo del
capítulo (que solo devuelven préstamos activos, `fechaDevolucion IS NULL`, según la
propia documentación de la interfaz), esta es la única que trae el historial completo,
activos y devueltos por igual, tal como pide el enunciado.

> **Nota:** sin `JOIN FETCH`, este método sufre el mismo riesgo N+1 y
> `LazyInitializationException` que la documentación de la interfaz advierte sobre las
> demás consultas, si se intenta acceder a `prestamo.getUsuario()`/`getLibro()` fuera de
> una transacción. Como el endpoint de abajo lo consume dentro de
> `PrestamoService`, anotado con `@Transactional` a nivel de clase, la sesión de
> Hibernate sigue abierta mientras `PrestamoRespuesta.desde(...)` accede a esos campos, así
> que no llega a fallar — pero conviene tenerlo presente: en un método que se llamara
> fuera de una transacción activa, haría falta añadir el mismo `JOIN FETCH` que ya usan
> las otras tres consultas.

`PrestamoService` con el método nuevo:

```java
@Transactional(readOnly = true)
public List<Prestamo> obtenerHistorialPorUsuario(Long usuarioId) {
    if (!usuarios.existsById(usuarioId)) {
        throw new UsuarioNoEncontradoException(usuarioId);
    }
    return prestamos.findByUsuarioIdOrderByFechaPrestamoDesc(usuarioId);
}
```

(Mismo patrón que `obtenerPorUsuario`: comprobar primero que el usuario existe, para
distinguir "usuario sin préstamos" —lista vacía, 200— de "usuario que no existe" —404—.)

`PrestamoController` con el endpoint nuevo:

```java
@GetMapping("/usuario/{usuarioId}/historial")
public List<PrestamoRespuesta> historialPorUsuario(@PathVariable Long usuarioId) {
    return service.obtenerHistorialPorUsuario(usuarioId).stream()
            .map(PrestamoRespuesta::desde)
            .toList();
}
```

### Diferencia con `GET /prestamos/usuario/{usuarioId}`

El endpoint que ya existía en el cuerpo del capítulo (`listarPorUsuario`, usando
`obtenerPorUsuario`) solo devuelve los préstamos **activos** de ese usuario —los libros
que tiene en su poder ahora mismo—, tal como documenta la propia interfaz
`PrestamoJpaRepository`. El nuevo `/historial` devuelve **todos**, incluidos los que ya
devolvió, ordenados del más reciente al más antiguo — el registro completo de la
actividad de préstamos de ese usuario, no solo su estado actual. Son dos preguntas
distintas ("¿qué tiene prestado ahora?" frente a "¿qué ha pedido prestado alguna vez?"),
y el volumen, tal como está, solo respondía a la primera hasta esta práctica.
