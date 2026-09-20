# Práctica 8.1 — Test para validación de autor nulo

## Enunciado

El campo `autor` de `Libro` tiene la restricción `@NotBlank`. Escribe un test que envíe
un POST con `"autor": null` y verifique que la respuesta es 400 y que el campo `title` de
la respuesta vale `"Datos de entrada no válidos"`.

## Solución

Nuevo método `@Test` en `LibroControllerTest`, con el mismo estilo que
`crearRechazaUnLibroSinTitulo` (que ya cubre el caso de `titulo` vacío):

```java
@Test
void crearRechazaUnLibroConAutorNulo() throws Exception {
    String json = "{\"titulo\":\"Dune\",\"autor\":null,\"anio\":1965}";

    mockMvc.perform(post("/libros")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Datos de entrada no válidos"));

    verify(service, never()).registrar(any());
}
```

Se construye el JSON como `String` literal, en vez de serializar un objeto `Libro` con
`objectMapper.writeValueAsString(...)`, porque un `Libro` en Java no puede representar un
`autor` que sea `null` de forma distinta a "no se ha asignado" — pero **sí** puede
representarlo un JSON escrito a mano con `"autor": null` explícito, que es justo el caso
que pide comprobar el enunciado (un cliente que manda el campo con valor nulo
explícitamente, no un campo ausente).

### Por qué falla con 400

`@NotBlank` en el campo `autor` de `Libro` rechaza tanto una cadena vacía (`""`) como
`null` (a diferencia de `@NotEmpty`, que aceptaría espacios en blanco, o de una simple
comprobación de "no vacío" que no cubriera `null`). Al deserializar
`{"titulo":"Dune","autor":null,"anio":1965}`, Jackson asigna `null` al campo `autor` del
objeto `Libro`; `@Valid` en el parámetro de `crear` dispara la validación antes de
ejecutar el cuerpo del método, `@NotBlank` la rechaza, y `MethodArgumentNotValidException`
llega a `manejarValidacion`, que responde 400 con `"title": "Datos de entrada no
válidos"` — el mismo mecanismo, y el mismo título, que ya verifica
`crearRechazaUnLibroSinTitulo` para el caso del título vacío.

`verify(service, never()).registrar(any())` comprueba, además, que la validación detiene
la petición **antes** de llegar al servicio: si `BibliotecaService.registrar(...)`
llegara a invocarse con datos inválidos, sería un fallo de la propia validación, no solo
un detalle menor — por eso vale la pena comprobarlo explícitamente y no solo el código de
estado.
