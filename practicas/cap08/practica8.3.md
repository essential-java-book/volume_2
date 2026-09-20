# Práctica 8.3 — Verificar el campo `Content-Type` de la respuesta

## Enunciado

Todos los endpoints de la API deberían devolver `Content-Type: application/json`. Añade a
los tests existentes de GET la verificación:

```java
.andExpect(content().contentType(MediaType.APPLICATION_JSON))
```

Comprueba que los tests de POST también verifican el `Content-Type` de la respuesta 201.

## Solución

Los dos tests de `GET` con la comprobación añadida:

```java
@Test
void listarTodosDevuelveElCatalogo() throws Exception {
    Libro elQuijote = new Libro(1L, "El Quijote", "Miguel de Cervantes", 1605);
    when(service.obtenerTodos()).thenReturn(List.of(elQuijote));

    mockMvc.perform(get("/libros"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].titulo").value("El Quijote"));
}

@Test
void obtenerPorIdDevuelveElLibroSiExiste() throws Exception {
    Libro libro = new Libro(1L, "El Quijote", "Miguel de Cervantes", 1605);
    when(service.buscarPorIdOFallar(1L)).thenReturn(libro);

    mockMvc.perform(get("/libros/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.titulo").value("El Quijote"));
}
```

Y el test de `POST` que crea correctamente (`crearRegistraUnLibroNuevo`), con la misma
comprobación sobre la respuesta `201`:

```java
@Test
void crearRegistraUnLibroNuevo() throws Exception {
    Libro entrada = new Libro(null, "Dune", "Frank Herbert", 1965);
    Libro creado = new Libro(4L, "Dune", "Frank Herbert", 1965);
    when(service.registrar(any(Libro.class))).thenReturn(creado);

    mockMvc.perform(post("/libros")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(entrada)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(4))
            .andExpect(jsonPath("$.titulo").value("Dune"));
}
```

### Qué comprueba realmente esta aserción

`content().contentType(MediaType.APPLICATION_JSON)` compara la cabecera `Content-Type`
real de la respuesta HTTP contra `application/json`. No es una comprobación redundante
con `jsonPath(...)`: `jsonPath` da por hecho que el cuerpo ya es JSON válido y navega
dentro de él, mientras que `content().contentType(...)` comprueba algo distinto —que el
servidor **declaró correctamente** el tipo de contenido en la cabecera, con el *charset*
que corresponda—, algo que un cliente HTTP real (un navegador, una librería de otro
lenguaje) sí necesita para decidir cómo interpretar la respuesta, y que un test que solo
mirara el cuerpo del JSON no llegaría a detectar si estuviera mal configurado.

En este proyecto, `Content-Type: application/json` sale "gratis" en todos los endpoints
que devuelven un objeto Java (Spring MVC + Jackson lo negocian automáticamente, sin
ninguna configuración manual), así que estos tests no deberían fallar — pero, si algún
día un endpoint devolviera texto plano por error, o un `ResponseEntity` mal construido
alterara la cabecera, esta comprobación lo detectaría inmediatamente, mientras que los
`jsonPath(...)` seguirían pasando siempre que el texto devuelto, por casualidad, siguiera
teniendo forma de JSON válido.

No se añade la comprobación a `crearRechazaUnLibroSinTitulo`, `crearRechazaUnLibroSinAnio`
ni a los tests de 404 y 400: el enunciado pide expresamente los de GET y el 201 de POST;
las respuestas de error también son JSON (el propio `ProblemDetail` se serializa con
`Content-Type: application/problem+json`, no `application/json` a secas), así que
añadirles la misma aserción literal habría hecho fallar esos tests por un motivo ajeno a
lo que pretendían comprobar.
