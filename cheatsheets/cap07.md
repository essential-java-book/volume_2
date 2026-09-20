# Cheat sheet · Capítulo 7 — Documentación de API con OpenAPI y Swagger UI

*Java Esencial · Volumen 2: Spring Boot · código del capítulo: tag `v2-cap07`*

## En una frase

Sabes generar la especificación OpenAPI de tu API directamente desde el código con springdoc, explorarla y probarla en Swagger UI, y enriquecerla con anotaciones en el controlador y en el modelo.

## Lo esencial

| Elemento | Para qué sirve |
|---|---|
| OpenAPI 3.1 | Estándar para describir una API REST en JSON o YAML: rutas, parámetros, esquemas y códigos de estado |
| `springdoc-openapi-starter-webmvc-ui` | Dependencia que genera la especificación y activa Swagger UI. No está en el BOM de Spring Boot: lleva `<version>` explícita (2.x para Spring Boot 3) |
| `/v3/api-docs` y `/v3/api-docs.yaml` | La especificación en JSON y en YAML |
| Swagger UI (`/swagger-ui.html`) | Interfaz visual que lee la especificación; con **Try it out** ejecutas peticiones reales |
| `@Tag` | Agrupa los endpoints de un controlador bajo un nombre en Swagger UI |
| `@Operation` | Resumen (`summary`) y descripción (`description`) de un endpoint |
| `@ApiResponses` / `@ApiResponse` | Documentan los códigos de estado que puede devolver una operación |
| `@Parameter` | Documenta un parámetro de ruta o de consulta |
| `@Schema` | Descripción, ejemplo y límites de la clase del modelo y de cada campo |
| `Schema.AccessMode.READ_ONLY` | Marca un campo (el `id`) como solo lectura: no se envía en POST ni PUT |
| Bean `OpenAPI` | Personaliza título, versión, descripción y contacto de la cabecera |
| `springdoc.swagger-ui.path` | Cambia la URL de Swagger UI (en el proyecto, `/docs`) |
| `springdoc.api-docs.path` | Ruta de la especificación en JSON |
| `springdoc.swagger-ui.enabled=false` | Deshabilita Swagger UI; junto con `springdoc.api-docs.enabled=false`, recomendado en producción |

## Código mínimo

La dependencia en el `pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

Un endpoint documentado en `LibroController`:

```java
@Tag(name = "Libros", description = "Catálogo de la Biblioteca")
@RestController
@RequestMapping("/libros")
public class LibroController {

    @Operation(summary = "Obtiene un libro por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe ningún libro con ese id")
    })
    @GetMapping("/{id}")
    public Libro obtenerPorId(@Parameter(description = "Id del libro") @PathVariable Long id) {
        return service.buscarPorIdOFallar(id);
    }
}
```

El modelo con `@Schema` en `Libro`:

```java
@Schema(description = "Identificador único", example = "1",
        accessMode = Schema.AccessMode.READ_ONLY)
private Long id;

@Schema(description = "Año de publicación", example = "1605",
        minimum = "1450", maximum = "2100")
@NotNull(message = "El año es obligatorio")
private Integer anio;
```

## Comandos y peticiones

```http
### Swagger UI (ruta del proyecto: springdoc.swagger-ui.path=/docs)
GET http://localhost:8080/docs

### Especificación OpenAPI en JSON
GET http://localhost:8080/v3/api-docs

### Especificación OpenAPI en YAML
GET http://localhost:8080/v3/api-docs.yaml
```

## Errores típicos

- **`NoClassDefFoundError: javax/servlet/http/HttpServletRequest` al arrancar** → estás usando springdoc 1.x (`springdoc-openapi-ui`), que es para Spring Boot 2. Cambia a `springdoc-openapi-starter-webmvc-ui` 2.x, que usa `jakarta.*`.
- **Swagger UI devuelve 404** → comprueba primero `/v3/api-docs`. Si tampoco responde, falta la dependencia; si responde, has cambiado la ruta con `springdoc.swagger-ui.path` y tienes que abrir `/docs`.
- **Aparecen campos con valor `null` en el JSON** → añade `spring.jackson.default-property-inclusion=non_null` o anota la clase con `@JsonInclude(JsonInclude.Include.NON_NULL)`.
- **El `id` sigue apareciendo como editable en el formulario de POST** → `readOnly = true` está obsoleto; usa `accessMode = Schema.AccessMode.READ_ONLY`.
- **Swagger UI accesible en producción** → en `application-prod.properties` pon `springdoc.swagger-ui.enabled=false` y `springdoc.api-docs.enabled=false`.

## En el Proyecto Biblioteca

Se añade la dependencia de springdoc al `pom.xml` y la clase nueva `OpenApiConfig` (paquete `configuracion`) con el bean `OpenAPI`. `LibroController` gana `@Tag`, `@Operation`, `@ApiResponses` y `@Parameter`, y `Libro` gana `@Schema` en la clase y en cada campo. En `application.properties` se fijan `springdoc.swagger-ui.path=/docs` y `springdoc.api-docs.path=/v3/api-docs`: la documentación queda en `http://localhost:8080/docs`.
