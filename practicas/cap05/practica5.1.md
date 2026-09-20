# Práctica 5.1 — Verificar todos los códigos de estado

## Enunciado

Ejecuta cada petición del fichero `pruebas.http` y completa la tabla:

| Petición | Código esperado | Código recibido |
|----------|-----------------|-----------------|
| `GET /libros` | 200 | |
| `GET /libros/1` | 200 | |
| `GET /libros/99` | 404 | |
| `POST /libros` (JSON válido) | 201 | |
| `PUT /libros/1` (JSON válido) | 200 | |
| `PUT /libros/99` | 404 | |
| `DELETE /libros/2` | 204 | |
| `DELETE /libros/99` | 404 | |

¿Coinciden todos los códigos? ¿Qué cuerpo devuelve el `DELETE` exitoso?

## Solución

Con el perfil `dev` activo (tres libros de partida vía `CargadorDatosPrueba`: id 1 = El
Quijote, id 2 = 1984, id 3 = Dune), y probando en este orden (importa: el `DELETE /libros/2`
elimina el libro que luego ya no aparecerá en `GET /libros`):

| Petición | Código esperado | Código recibido | Coincide |
|----------|-----------------|-----------------|----------|
| `GET /libros` | 200 | **200** | Sí |
| `GET /libros/1` | 200 | **200** | Sí |
| `GET /libros/99` | 404 | **404** | Sí |
| `POST /libros` (JSON válido) | 201 | **201** | Sí |
| `PUT /libros/1` (JSON válido) | 200 | **200** | Sí |
| `PUT /libros/99` | 404 | **404** | Sí |
| `DELETE /libros/2` | 204 | **204** | Sí |
| `DELETE /libros/99` | 404 | **404** | Sí |

Los ocho códigos coinciden con lo esperado. Se explican directamente con el código de
`LibroController` (§5.3-5.6):

- `listarTodos()` y `obtenerPorId(id)` no declaran código explícito: Spring usa **200**
  por defecto en cualquier respuesta correcta que no sea `void`. `obtenerPorId(99)` no
  encuentra el libro (`buscarPorId` devuelve `Optional.empty()`), así que cae en la rama
  `.orElse(ResponseEntity.notFound().build())` → **404**.
- `crear(...)` responde siempre `ResponseEntity.status(HttpStatus.CREATED)` → **201**,
  sin condición: cualquier `POST` con JSON válido crea un libro nuevo.
- `actualizar(id, libro)` sigue el mismo patrón `map(...).orElse(notFound())` que
  `obtenerPorId`: **200** si el id existe (`PUT /libros/1`), **404** si no
  (`PUT /libros/99`).
- `eliminar(id)` comprueba el `boolean` que devuelve `repositorio.eliminar(id)`:
  **204** (`noContent()`) si borró algo de verdad (`DELETE /libros/2`, el libro `1984`
  existía), **404** si no había nada que borrar con ese id (`DELETE /libros/99`).

### Cuerpo del `DELETE` exitoso

**Vacío.** `eliminar` devuelve `ResponseEntity<Void>`, y `ResponseEntity.noContent()`
construye explícitamente una respuesta **204 No Content**: por definición del propio
código de estado, un 204 no lleva cuerpo — es la forma estándar de decir "la operación
salió bien y no hay nada que devolverte", frente al 200 de una lectura o el 201 de una
creación, que sí devuelven el recurso en el cuerpo de la respuesta.
