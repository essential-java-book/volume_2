# Práctica 9.4 — Test con @DataJpaTest

## Enunciado

`@DataJpaTest` es la contraparte de `@WebMvcTest` para la capa de persistencia: carga
solo el contexto JPA con una H2 en memoria. Crea la clase `LibroJpaRepositoryTest` y
escribe un test que guarde un `Libro` con `repositorio.save(...)` y luego lo recupere con
`repositorio.findById(...)` verificando que el título coincide.

## Solución

```java
package com.javaesencial.biblioteca.repositorio;

import com.javaesencial.biblioteca.dominio.Libro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración de la capa de persistencia, aislado del resto
 * de la aplicación con {@code @DataJpaTest}: solo se carga el
 * contexto JPA (entidades, repositorios Spring Data) contra una base
 * H2 en memoria, sin controladores ni capa web.
 */
@DataJpaTest
class LibroJpaRepositoryTest {

    @Autowired
    private LibroJpaRepository repositorio;

    @Test
    void guardaYRecuperaUnLibroPorId() {
        Libro nuevo = new Libro(null, "Fahrenheit 451", "Ray Bradbury", 1953);

        Libro guardado = repositorio.save(nuevo);
        Optional<Libro> recuperado = repositorio.findById(guardado.getId());

        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getTitulo()).isEqualTo("Fahrenheit 451");
    }
}
```

### Qué hace `@DataJpaTest` de forma distinta a `@WebMvcTest` y al test unitario

Comparando los tres tipos de test que ya tiene el proyecto a estas alturas:

| | `@WebMvcTest` (Cap. 8) | Test unitario con Mockito (Cap. 8) | `@DataJpaTest` (esta práctica) |
|---|---|---|---|
| Contexto de Spring | Sí, capa web solamente | No, ninguno | Sí, capa JPA solamente |
| Base de datos real | No interviene | No interviene | Sí, H2 en memoria de verdad |
| Qué sustituye por un doble | `BibliotecaService` (`@MockBean`) | `RepositorioLibros`/`LibroJpaRepository` (`@Mock`) | Nada — `LibroJpaRepository` es real |
| Qué verifica | El controlador traduce bien HTTP ↔ Java | La lógica del servicio en aislamiento | Que las consultas JPA funcionan de verdad contra SQL |

`@DataJpaTest` configura automáticamente una base de datos H2 en memoria (independiente de
la que usa la aplicación al arrancar normalmente, aunque comparta motor), aplica
`data.sql` igual que en un arranque normal, y por defecto **envuelve cada test en una
transacción que se deshace al terminar** — así, `guardaYRecuperaUnLibroPorId` no deja
residuos: aunque inserte un libro nuevo, ese `INSERT` se revierte automáticamente al
acabar el test, y el siguiente test (o el próximo arranque real de la aplicación) no lo
ve.

### Por qué este test sí necesita `@Autowired` y una base de datos real

A diferencia del test unitario de la Práctica 8.4 (que sustituía el repositorio por un
`@Mock` para aislar la lógica de `BibliotecaService`), aquí el propio objetivo es
comprobar que **el repositorio real funciona** — que `save(...)` genera el `INSERT` SQL
correcto, que el `id` autogenerado (`@GeneratedValue(strategy =
GenerationType.IDENTITY)`) se asigna de verdad, y que `findById(...)` lo recupera
correctamente desde la base de datos. Sustituir `LibroJpaRepository` por un doble aquí no
tendría sentido: sería un test que no comprueba nada real sobre JPA, solo sobre un objeto
simulado que ya se ha programado para responder lo esperado.
