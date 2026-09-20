# Cheat sheet · Capítulo 8 — Tests de API con MockMvc

*Java Esencial · Volumen 2: Spring Boot · código del capítulo: tag `v2-cap08`*

## En una frase

Sabes escribir tests automatizados del controlador con `@WebMvcTest` y `MockMvc`, simulando el servicio con `@MockBean`, sin arrancar un servidor real.

## Lo esencial

| Elemento | Para qué sirve |
|---|---|
| `spring-boot-starter-test` | Starter (alcance `test`) con JUnit 5, Mockito, MockMvc, AssertJ y Jackson. Ya viene en el `pom.xml` de Spring Initializr |
| Test unitario | Prueba una clase aislada, sin Spring (JUnit 5 + Mockito) |
| Test de integración de capa | Prueba el controlador con Spring MVC simulado (`@WebMvcTest` + `MockMvc`) |
| Test de extremo a extremo | Prueba la aplicación completa (`@SpringBootTest`); es el más lento |
| `@WebMvcTest(LibroController.class)` | Carga solo la capa web para ese controlador: ni servicios, ni repositorios, ni base de datos |
| `MockMvc` | Envía peticiones HTTP simuladas: `perform(get(...))`, `post`, `put`, `delete` |
| `@MockBean` | Registra un mock de Mockito como bean; el controlador lo recibe en lugar del servicio real |
| `ObjectMapper` | Convierte objetos Java en JSON para el cuerpo de la petición (`writeValueAsString`) |
| `when(...).thenReturn(...)` / `thenThrow(...)` | Configura qué devuelve o qué lanza el mock |
| `doThrow(...).when(mock).metodo(...)` | Lo mismo para métodos `void`, como `eliminar` |
| `any(Libro.class)` / `eq(1L)` | Matchers de Mockito: cualquier instancia del tipo / solo ese valor exacto |
| `verify(service).eliminar(1L)` / `verify(service, never())` | Comprueba que el mock se llamó, o que no se llamó |
| `andExpect(status().isOk())` | Verifica el código de estado (`isCreated`, `isBadRequest`, `isNotFound`, `isNoContent`) |
| `jsonPath("$[0].titulo")`, `jsonPath("$.length()")` | Verifica campos del JSON: `$` es la raíz, `$[0]` el primer elemento del array |

## Código mínimo

El esqueleto de la clase de test (`src/test/java/com/javaesencial/biblioteca/controlador`):

```java
@WebMvcTest(LibroController.class)
class LibroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BibliotecaService service;
}
```

Un POST válido, con la convención `debería_[resultado]_cuando[Condición]`:

```java
@Test
void debería_devolver201_cuandoLibroEsVálido() throws Exception {
    Libro entrada = new Libro(null, "Dune", "Frank Herbert", 1965);
    Libro creado = new Libro(4L, "Dune", "Frank Herbert", 1965);
    when(service.registrar(any(Libro.class))).thenReturn(creado);

    mockMvc.perform(post("/libros")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(entrada)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(4))
            .andExpect(jsonPath("$.titulo").value("Dune"));
}
```

Un 404 cuando el servicio lanza la excepción:

```java
@Test
void debería_devolver404_cuandoNoExisteElId() throws Exception {
    when(service.buscarPorIdOFallar(99L))
            .thenThrow(new LibroNoEncontradoException(99L));

    mockMvc.perform(get("/libros/99"))
            .andExpect(status().isNotFound());
}
```

## Comandos y peticiones

```bash
# Ejecutar todos los tests del proyecto
mvn test

# Ejecutar solo la clase LibroControllerTest
mvn test -Dtest=LibroControllerTest
```

## Errores típicos

- **`NoSuchBeanDefinitionException` para `BibliotecaService` al arrancar el test** → `@WebMvcTest` no carga el servicio real; declara un `@MockBean` por cada dependencia del controlador.
- **`Body = null` y `No value at JSON path "$.titulo"`** → no has configurado el mock y Mockito devuelve `null` por defecto; pon el `when(...).thenReturn(...)` antes de la petición.
- **Status 415, `Content-Type 'application/octet-stream' not supported`** → añade `.contentType(MediaType.APPLICATION_JSON)` en toda petición con cuerpo (POST y PUT).
- **Los tests tardan segundos o fallan buscando la base de datos** → estás usando `@SpringBootTest`; para testear solo el controlador usa `@WebMvcTest` y reserva `@SpringBootTest` para el sistema completo.
- **`JSON path "$.titulo"` devuelve `null` en GET /libros** → la respuesta es un array; indexa primero: `$[0].titulo`, y usa `$.length()` para el tamaño.

## En el Proyecto Biblioteca

Se añade `LibroControllerTest` en `src/test/java/com/javaesencial/biblioteca/controlador`, con diez tests que cubren el CRUD: listado, búsqueda por id (200 y 404), creación (201 y 400), actualización (200 y 404) y borrado (204 y 404). El código de producción no cambia. Al ejecutar `mvn test` debes ver `Tests run: 10, Failures: 0, Errors: 0, Skipped: 0`.
