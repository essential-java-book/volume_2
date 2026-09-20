# Práctica 8.4 — Test unitario del servicio

## Enunciado

`@WebMvcTest` testa la capa del controlador. Escribe ahora un **test unitario** (sin
Spring) para `BibliotecaService`. Usa `@ExtendWith(MockitoExtension.class)` para activar
Mockito y `@Mock` para simular `RepositorioLibros`. Escribe un test que verifique que
`obtenerTodos()` delega la llamada al repositorio y devuelve su resultado.

## Solución

Clase nueva, `BibliotecaServiceTest`, junto a `LibroControllerTest` pero en el paquete
`servicio/` (el mismo paquete que la clase que testea):

```java
package com.javaesencial.biblioteca.servicio;

import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.repositorio.RepositorioLibros;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitario de {@code BibliotecaService}, sin levantar ningún
 * contexto de Spring: {@code RepositorioLibros} se sustituye por un
 * doble de Mockito, así que la única clase real bajo prueba es
 * {@code BibliotecaService} en sí misma.
 */
@ExtendWith(MockitoExtension.class)
class BibliotecaServiceTest {

    @Mock
    private RepositorioLibros repositorio;

    @InjectMocks
    private BibliotecaService service;

    @Test
    void obtenerTodosDelegaEnElRepositorioYDevuelveSuResultado() {
        List<Libro> catalogo = List.of(
                new Libro(1L, "El Quijote", "Miguel de Cervantes", 1605),
                new Libro(2L, "1984", "George Orwell", 1949)
        );
        when(repositorio.findAll()).thenReturn(catalogo);

        List<Libro> resultado = service.obtenerTodos();

        assertThat(resultado).isEqualTo(catalogo);
        verify(repositorio).findAll();
    }
}
```

### Diferencias clave frente a `LibroControllerTest`

`@WebMvcTest(LibroController.class)` (Capítulo 8, sección 8.3) **sí** levanta un contexto
de Spring —reducido, solo la capa web—, con `MockMvc` simulando peticiones HTTP reales
contra un `DispatcherServlet` de verdad; por eso hacía falta `@MockBean` para sustituir
`BibliotecaService` dentro de ese contexto. Aquí, con `@ExtendWith(MockitoExtension.class)`,
**no hay ningún contexto de Spring en absoluto**: `BibliotecaService` se instancia como un
objeto Java normal, y `@InjectMocks` es quien se encarga de construirlo pasándole el
`@Mock` de `RepositorioLibros` por su constructor (el mismo constructor que en producción
rellena Spring) — sin `ApplicationContext`, sin escaneo de componentes, sin arrancar nada
que no sea la propia JVM. Es, con diferencia, el test más rápido de todos los que tiene el
proyecto hasta ahora: no paga el coste de levantar ni siquiera el contexto reducido de
`@WebMvcTest`.

`@Mock` crea un doble de `RepositorioLibros` que no ejecuta ninguna lógica real —solo
responde lo que se le indique con `when(...)`—; `@InjectMocks` es quien conecta ese doble
con la instancia real de `BibliotecaService` que se está probando.

### Qué demuestra el test

`when(repositorio.findAll()).thenReturn(catalogo)` prepara el doble para que, cuando se
le pregunte, devuelva una lista fija conocida. `service.obtenerTodos()` se llama de
verdad, sobre el objeto real de `BibliotecaService` (no un doble). El `assertThat(...)`
comprueba que el resultado que llega hasta fuera es exactamente esa misma lista —es
decir, que `obtenerTodos()` no transforma ni filtra nada, solo delega—, y
`verify(repositorio).findAll()` comprueba, además, que el método del repositorio
realmente se invocó (y no que el resultado coincidiera por casualidad con otro camino de
código). Juntas, las dos aserciones son la definición operativa de "delega la llamada y
devuelve su resultado" que pide el enunciado.
