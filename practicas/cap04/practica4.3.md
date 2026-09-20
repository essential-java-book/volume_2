# Práctica 4.3 — Bean condicional con `@Profile`

## Enunciado

Crea un bean `CargadorDatosPrueba` que solo se active con el perfil `dev`:

```java
@Component
@Profile("dev")
public class CargadorDatosPrueba {
    // Carga 5 libros adicionales al arrancar
}
```

1. Con el perfil `dev` activo, ¿cuántos libros aparecen en `GET /libros`?
2. Con el perfil `prod` activo, ¿cuántos libros aparecen? ¿Por qué?
3. ¿Qué ventaja tiene `@Profile` frente a un `if (perfil.equals("dev"))` en el código?

## Solución

> **Nota:** en el proyecto real, este mismo mecanismo —un bean `@Profile("dev")` que
> carga datos de prueba— es el que sustituye por completo al antiguo
> `RepositorioLibros.cargarDatosIniciales()` con `@PostConstruct` que traían los
> Capítulos 1-3: desde este capítulo, `RepositorioLibros` nace **vacío** siempre (también
> en `dev`), y es `CargadorDatosPrueba` quien decide si lo rellena o no, según el perfil
> activo. Por eso la solución de abajo carga los mismos tres libros de siempre (El
> Quijote, 1984, Dune) en vez de "5 libros" arbitrarios: son los datos de prueba reales
> del proyecto a partir de ahora, no un añadido aparte.

`RepositorioLibros` pierde su `@PostConstruct` y gana un método para insertar libros uno
a uno:

```java
package com.javaesencial.biblioteca.repositorio;

import com.javaesencial.biblioteca.dominio.Libro;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RepositorioLibros {

    private final List<Libro> libros = new ArrayList<>();

    public List<Libro> findAll() {
        return libros;
    }

    public Optional<Libro> findById(Long id) {
        return libros.stream()
                .filter(libro -> libro.getId().equals(id))
                .findFirst();
    }

    public Libro agregar(Libro libro) {
        libros.add(libro);
        return libro;
    }
}
```

`CargadorDatosPrueba`, nuevo, en `configuracion/` (junto a `BibliotecaConfig`):

```java
package com.javaesencial.biblioteca.configuracion;

import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.repositorio.RepositorioLibros;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Datos de prueba del catálogo, solo activos con el perfil {@code dev}.
 * En producción el repositorio nace vacío (la persistencia real llega
 * en el Capítulo 9).
 */
@Component
@Profile("dev")
public class CargadorDatosPrueba {

    private final RepositorioLibros repositorio;

    public CargadorDatosPrueba(RepositorioLibros repositorio) {
        this.repositorio = repositorio;
    }

    @PostConstruct
    public void cargar() {
        repositorio.agregar(new Libro(1L, "El Quijote", "Miguel de Cervantes", 1605));
        repositorio.agregar(new Libro(2L, "1984", "George Orwell", 1949));
        repositorio.agregar(new Libro(3L, "Dune", "Frank Herbert", 1965));
    }
}
```

### 1. Con `dev` activo

`GET /libros` devuelve **3 libros** (El Quijote, 1984, Dune): con `dev` activo, Spring
crea el bean `CargadorDatosPrueba` (su condición `@Profile("dev")` se cumple), y su
`@PostConstruct` los añade al repositorio nada más arrancar, igual que hacía antes
`RepositorioLibros` por sí mismo.

### 2. Con `prod` activo

`GET /libros` devuelve **una lista vacía**, `[]`. Con `prod` activo, la condición
`@Profile("dev")` no se cumple: Spring **ni siquiera crea** el bean
`CargadorDatosPrueba` — no es que se cree y no haga nada, es que la clase entera queda
fuera del contexto de Spring, como si no existiera. `RepositorioLibros` sigue naciendo
como una lista vacía (`new ArrayList<>()`) y, sin nadie que la rellene, se queda así:
un repositorio real de producción no debe arrancar con datos de prueba inventados.

### 3. Ventaja de `@Profile` frente a un `if` en el código

Con un `if (perfil.equals("dev")) { ... }`, el código de carga de datos de prueba
**viajaría siempre** dentro del JAR de producción, aunque nunca llegara a ejecutarse: la
clase se compilaría, se empaquetaría y se cargaría en memoria en cualquier entorno, y
solo una comprobación en tiempo de ejecución evitaría que se disparara. Eso es un riesgo
en sí mismo — un error en esa condición (una variable de entorno mal puesta, un valor por
defecto equivocado) podría dejar datos de prueba en producción sin que nada lo impidiera
a nivel estructural.

Con `@Profile("dev")`, la decisión la toma **Spring, al construir el contexto**, antes de
que el bean llegue a existir: en `prod`, la clase `CargadorDatosPrueba` no se instancia
en ningún momento, así que no hay ninguna posibilidad de que sus datos de prueba lleguen
al repositorio por accidente. Es el mismo argumento de fondo que ya apareció en la
Práctica 4.1 y en la sección 4.6 del capítulo: declarar la condición de forma que el
propio framework la garantice es más seguro que confiar en que una comprobación manual en
tiempo de ejecución se acuerde siempre de hacerse bien.
