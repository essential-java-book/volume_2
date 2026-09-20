# Práctica 8.2 — Test de año máximo

## Enunciado

Escribe un test que envíe un POST con `"anio": 2200` (superior al máximo de 2100) y
verifique que la respuesta es 400 y que el campo `title` de la respuesta vale `"Datos de
entrada no válidos"`.

## Solución

```java
@Test
void crearRechazaUnLibroConAnioSuperiorAlMaximo() throws Exception {
    Libro anioFuturo = new Libro(null, "Libro del futuro", "Autor de prueba", 2200);

    mockMvc.perform(post("/libros")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(anioFuturo)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Datos de entrada no válidos"));

    verify(service, never()).registrar(any());
}
```

A diferencia de la Práctica 8.1, aquí sí se puede construir un `Libro` normal y
serializarlo con `objectMapper.writeValueAsString(...)` (como ya hace
`crearRegistraUnLibroNuevo`): `anio` es un `Integer` con un valor concreto (2200), no
`null`, así que no hace falta escribir el JSON a mano.

### Por qué falla con 400

`Libro.anio` lleva `@Max(value = 2100, message = "El año no puede ser posterior a
2100")`. 2200 supera ese máximo, así que `@Valid` rechaza la petición exactamente igual
que en la Práctica 8.1 —mismo mecanismo, mismo `manejarValidacion`, mismo `"title":
"Datos de entrada no válidos"`—, solo que aquí la restricción que salta es `@Max` en vez
de `@NotBlank`. Con este test y el de la Práctica 8.1 juntos, quedan cubiertas dos
restricciones distintas del mismo `Libro` (una de "campo obligatorio" y otra de "rango de
valor"), reforzando que `manejarValidacion` responde de forma consistente —mismo
`title`, código 400— sea cual sea la regla concreta que se incumpla, sin tener que
escribir un test distinto por cada tipo de mensaje.
