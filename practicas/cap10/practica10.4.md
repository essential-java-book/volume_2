# Práctica 10.4 — Test de PrestamoController con @WebMvcTest

## Enunciado

Crea `PrestamoControllerTest` con `@WebMvcTest(PrestamoController.class)`. Escribe un
test que verifique que `POST /prestamos/usuario/1/libro/1` devuelve 201 y contiene los
campos `usuarioId`, `libroId` y `fechaPrestamo` en el JSON de respuesta. Usa `@MockBean`
para simular `PrestamoService`.

## Solución

```java
package com.javaesencial.biblioteca.controlador;

import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.dominio.Prestamo;
import com.javaesencial.biblioteca.dominio.Usuario;
import com.javaesencial.biblioteca.servicio.PrestamoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Suite de tests de la capa REST de {@code PrestamoController}, aislada
 * del resto de la aplicación con {@code @WebMvcTest} (igual que
 * {@code LibroControllerTest} en el Capítulo 8): solo se carga el
 * controlador y el contexto MVC; {@code PrestamoService} se sustituye
 * por un doble con {@code @MockBean}.
 */
@WebMvcTest(PrestamoController.class)
class PrestamoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrestamoService service;

    @Test
    void registrarCreaUnPrestamoYDevuelveLosCamposEsperados() throws Exception {
        Usuario usuario = new Usuario(1L, "Ana García", "ana@example.com");
        Libro libro = new Libro(1L, "El Quijote", "Miguel de Cervantes", 1605);
        Prestamo prestamo = new Prestamo(usuario, libro, LocalDate.now());
        prestamo.setId(10L);

        when(service.registrar(eq(1L), eq(1L))).thenReturn(prestamo);

        mockMvc.perform(post("/prestamos/usuario/1/libro/1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.libroId").value(1))
                .andExpect(jsonPath("$.fechaPrestamo").exists());
    }
}
```

### Por qué el doble devuelve un `Prestamo`, no un `PrestamoRespuesta`

`PrestamoService.registrar(usuarioId, libroId)` devuelve un `Prestamo` (la entidad JPA),
no el DTO — es `PrestamoController.registrar` quien lo convierte llamando a
`PrestamoRespuesta.desde(...)`, después de recibirlo del servicio. Por eso el
`when(...).thenReturn(...)` del test también tiene que devolver un `Prestamo`: el doble
sustituye exactamente lo que el servicio real devolvería, y es el controlador real (no
simulado, porque `@WebMvcTest` solo sustituye el *servicio*) quien se encarga de la
conversión — este test, de hecho, verifica de forma indirecta que esa conversión
(`PrestamoRespuesta.desde`) funciona bien, porque comprueba los campos del JSON final
después de pasar por ella.

`prestamo.setId(10L)` es necesario porque el constructor de `Prestamo` usado
(`new Prestamo(usuario, libro, fechaPrestamo)`) no recibe el id —lo asignaría la base de
datos real, vía `@GeneratedValue`, en un `save(...)` de verdad—; aquí, al ser un objeto de
prueba construido a mano, hay que fijarlo explícitamente para que `PrestamoRespuesta.id`
no quede a `null`.

### Qué comprueba `jsonPath("$.fechaPrestamo").exists()`

En vez de comprobar un valor exacto (como si fuera `.value("2026-09-11")`), esta aserción
solo comprueba que el campo **está presente** en el JSON de respuesta, sin fijar una
fecha concreta — apropiado aquí porque el propio test usa `LocalDate.now()` para construir
el préstamo de prueba, así que el valor exacto cambia cada día que se ejecute el test; lo
que importa comprobar es que el campo `fechaPrestamo` existe y viaja en la respuesta, tal
como pide el enunciado, no una fecha fija que haría el test frágil sin necesidad.
