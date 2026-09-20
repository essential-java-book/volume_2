# Práctica 3.3 — Búsqueda por autor con `@RequestParam`

## Enunciado

Añade a `LibroController` un endpoint de búsqueda que filtre libros por autor:

```java
@GetMapping("/libros/buscar")
public List<Libro> buscarPorAutor(@RequestParam(required = false) String autor) {
    if (autor == null || autor.isBlank()) {
        return service.obtenerTodos();
    }
    return service.obtenerTodos().stream()
            .filter(l -> l.getAutor().toLowerCase().contains(autor.toLowerCase()))
            .toList();
}
```

1. Prueba con `GET http://localhost:8080/libros/buscar?autor=martin`.
2. Prueba con `GET http://localhost:8080/libros/buscar` sin parámetro. ¿Qué devuelve?
3. ¿Por qué se usa `required = false`? ¿Qué ocurriría sin esa opción?
4. Mueve la lógica de filtrado a `BibliotecaService` añadiendo el método
   `buscarPorAutor(String autor)`. ¿Por qué es mejor que la lógica de negocio esté en el
   servicio y no en el controlador?

## Solución

> **Nota sobre la ruta:** `LibroController` no lleva `@RequestMapping("/libros")` a nivel
> de clase (cada método declara su ruta completa, como ya hace `listarTodos()` con
> `@GetMapping("/libros")`); por eso el nuevo endpoint tiene que escribirse como
> `@GetMapping("/libros/buscar")` completo, y no solo `"/buscar"`.

`LibroController` con el endpoint de búsqueda añadido:

```java
package com.javaesencial.biblioteca.controlador;

import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.servicio.BibliotecaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LibroController {

    private final BibliotecaService service;

    public LibroController(BibliotecaService service) {
        this.service = service;
    }

    @GetMapping("/libros")
    public List<Libro> listarTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/libros/buscar")
    public List<Libro> buscarPorAutor(@RequestParam(required = false) String autor) {
        return service.buscarPorAutor(autor);
    }
}
```

### 1. `GET /libros/buscar?autor=martin`

Con los tres libros que carga `RepositorioLibros.cargarDatosIniciales()` (El Quijote /
Cervantes, 1984 / Orwell, Dune / Herbert), ninguno tiene "martin" en el autor, así que la
respuesta es una lista vacía:

```json
[]
```

(Si se probara con `autor=orwell` o `autor=Orwell`, sí devolvería `1984`: el filtro
compara en minúsculas con `toLowerCase()`, así que no importan mayúsculas/minúsculas ni
si se busca el nombre completo o solo parte de él, gracias a `contains`.)

### 2. `GET /libros/buscar` sin parámetro

Devuelve los tres libros completos — el mismo resultado que `GET /libros`:

```json
[
  { "id": 1, "titulo": "El Quijote", "autor": "Miguel de Cervantes", "anio": 1605 },
  { "id": 2, "titulo": "1984", "autor": "George Orwell", "anio": 1949 },
  { "id": 3, "titulo": "Dune", "autor": "Frank Herbert", "anio": 1965 }
]
```

Porque `autor` llega como `null` cuando no se manda el parámetro, y la comprobación
`autor == null || autor.isBlank()` hace que se devuelvan todos los libros sin filtrar.

### 3. Por qué `required = false`

Por defecto, `@RequestParam` exige que el parámetro esté presente en la URL; si faltara
sin `required = false`, Spring respondería **400 Bad Request** con un mensaje del tipo
`Required request parameter 'autor' for method parameter type String is not present`,
antes siquiera de llegar a ejecutar el cuerpo del método. `required = false` hace que el
parámetro sea opcional: si no está, `autor` llega como `null` en vez de provocar un
error, y el propio método decide qué hacer con esa ausencia (aquí, devolver la lista
completa) — así, el mismo endpoint sirve tanto para "dame todos" como para "dame los que
coincidan con este autor".

### 4. Lógica de filtrado movida a `BibliotecaService`

```java
package com.javaesencial.biblioteca.servicio;

import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.repositorio.RepositorioLibros;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BibliotecaService {

    private final RepositorioLibros repositorio;

    public BibliotecaService(RepositorioLibros repositorio) {
        this.repositorio = repositorio;
    }

    public List<Libro> obtenerTodos() {
        return repositorio.findAll();
    }

    public Optional<Libro> buscarPorId(Long id) {
        return repositorio.findById(id);
    }

    public List<Libro> buscarPorAutor(String autor) {
        if (autor == null || autor.isBlank()) {
            return obtenerTodos();
        }
        return obtenerTodos().stream()
                .filter(l -> l.getAutor().toLowerCase().contains(autor.toLowerCase()))
                .toList();
    }
}
```

Con el controlador reducido a delegar: `return service.buscarPorAutor(autor);`.

Que la lógica de negocio viva en el servicio y no en el controlador es mejor por varias
razones concretas: el controlador queda centrado en su única responsabilidad real
—traducir HTTP (parámetros de la URL, códigos de estado) hacia y desde llamadas a
Java normales—, sin mezclar reglas de filtrado; la lógica de `buscarPorAutor` queda
reutilizable desde cualquier otro punto de la aplicación que la necesite (otro
controlador, un *job* programado, un test) sin depender de que exista una petición HTTP
de por medio; y es más fácil de testear de forma aislada, con un test de servicio
normal (sin `MockMvc` ni levantar el contexto web), tal como se hará con
`@WebMvcTest` en el Capítulo 8 para el controlador y con tests de servicio "a secas" para
la lógica de negocio.
