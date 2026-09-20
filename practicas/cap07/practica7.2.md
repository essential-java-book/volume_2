# Práctica 7.2 — Añadir ejemplos con @ExampleObject

## Enunciado

La anotación `@io.swagger.v3.oas.annotations.media.Content` junto con `@ExampleObject`
permite mostrar un ejemplo JSON completo en Swagger UI. Modifica `@ApiResponse` del
endpoint `GET /libros` para incluir un ejemplo de respuesta con tres libros reales del
catálogo.

## Solución

`listarTodos()`, antes de la práctica, solo lleva `@Operation(summary = ...)`, sin
`@ApiResponse` explícito. Con el ejemplo añadido, usando los tres libros de partida del
catálogo (El Quijote, 1984, Dune):

```java
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@Operation(summary = "Lista todos los libros del catálogo")
@ApiResponse(
        responseCode = "200",
        description = "Listado completo del catálogo",
        content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                        name = "Catálogo de ejemplo",
                        value = """
                                [
                                  { "id": 1, "titulo": "El Quijote", "autor": "Miguel de Cervantes", "anio": 1605 },
                                  { "id": 2, "titulo": "1984", "autor": "George Orwell", "anio": 1949 },
                                  { "id": 3, "titulo": "Dune", "autor": "Frank Herbert", "anio": 1965 }
                                ]
                                """
                )
        )
)
@GetMapping
public List<Libro> listarTodos() {
    return service.obtenerTodos();
}
```

(El texto JSON del ejemplo se escribe como *text block* de Java, `"""..."""`, cómodo para
JSON multilínea sin tener que escapar comillas manualmente línea a línea.)

### Comprobación en Swagger UI

Al expandir `GET /libros` y desplegar la respuesta `200`, junto al *schema* inferido
automáticamente (la forma genérica de un `Libro[]`, con los tipos de cada campo) aparece
ahora una pestaña "Example Value" adicional —o directamente el ejemplo, según la versión
de Swagger UI— con el JSON completo de tres libros reales, tal como lo definió
`@ExampleObject`. La diferencia práctica frente al *schema* solo: el *schema* dice "esto
es un array de objetos con estos campos y estos tipos"; el ejemplo dice, además, "esto es
exactamente lo que te vas a encontrar en un caso real" — mucho más rápido de leer para
quien solo quiere entender de un vistazo qué forma tienen los datos, sin tener que
imaginarse valores concretos a partir de los tipos.
