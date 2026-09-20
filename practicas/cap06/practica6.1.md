# Práctica 6.1 — Añadir validación al campo `titulo`

## Enunciado

La restricción actual de `titulo` solo verifica que no esté vacío. Añade las siguientes
reglas adicionales:

- El título debe tener al menos 2 caracteres.
- No puede superar los 300 caracteres.
- Si el título tiene exactamente 1 carácter, el mensaje de error debe ser: `"El título
  debe tener al menos 2 caracteres"`.

Prueba los tres casos con el archivo `pruebas.http`.

## Solución

El campo `titulo` en `Libro`, tal como queda al terminar el cuerpo del capítulo, es:

```java
@NotBlank(message = "El título es obligatorio")
@Size(min = 1, max = 200, message = "El título no puede superar los 200 caracteres")
private String titulo;
```

Con las reglas de la práctica (mínimo 2, máximo 300, mensaje específico para el caso
límite de 1 carácter):

```java
@NotBlank(message = "El título es obligatorio")
@Size(min = 2, max = 300, message = "El título debe tener al menos 2 caracteres")
private String titulo;
```

### Por qué un único mensaje cubre los dos límites

`@Size(min, max)` valida un único rango con un único mensaje: no hay una anotación
separada para "demasiado corto" y otra para "demasiado largo". El enunciado solo exige un
mensaje concreto para el caso de 1 carácter (justo por debajo del mínimo), así que basta
con que el mensaje de `@Size` sea ese; si se quisiera un mensaje distinto y más preciso
para el caso de "demasiado largo" (300+ caracteres), haría falta una validación
personalizada aparte (fuera del alcance de esta práctica).

### Prueba de los tres casos con `pruebas.http`

**Caso 1 — título de 1 carácter** (viola el mínimo):

```http
POST http://localhost:8080/libros
Content-Type: application/json

{
  "titulo": "X",
  "autor": "Autor de prueba",
  "anio": 2000
}
```

```terminal
HTTP/1.1 400
```
```json
{
  "type": "about:blank",
  "title": "Datos de entrada no válidos",
  "status": 400,
  "detail": "titulo: El título debe tener al menos 2 caracteres"
}
```

**Caso 2 — título de 2 caracteres** (el mínimo exacto, válido):

```http
POST http://localhost:8080/libros
Content-Type: application/json

{
  "titulo": "El",
  "autor": "Autor de prueba",
  "anio": 2000
}
```

```terminal
HTTP/1.1 201
```

Se crea sin error: 2 caracteres cumple `min = 2` (el límite es inclusivo).

**Caso 3 — título de 301 caracteres** (viola el máximo):

```http
POST http://localhost:8080/libros
Content-Type: application/json

{
  "titulo": "Lorem ipsum ... (301 caracteres) ...",
  "autor": "Autor de prueba",
  "anio": 2000
}
```

```terminal
HTTP/1.1 400
```
```json
{
  "type": "about:blank",
  "title": "Datos de entrada no válidos",
  "status": 400,
  "detail": "titulo: El título debe tener al menos 2 caracteres"
}
```

Este tercer caso es el que expone la limitación del mensaje único señalada arriba: un
título de 301 caracteres recibe el mismo texto de error ("El título debe tener al menos
2 caracteres"), aunque el problema real sea justo el contrario — demasiado largo, no
demasiado corto. Es un ejemplo real de por qué, en un sistema de producción, conviene
mensajes distintos por tipo de violación (con validaciones personalizadas), algo que este
capítulo no cubre pero que vale la pena tener presente.
