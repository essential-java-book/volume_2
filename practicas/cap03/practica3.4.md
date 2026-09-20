# Práctica 3.4 — `EstadisticasService`: un segundo servicio

## Enunciado

Crea un `record ResumenBiblioteca(int totalLibros, int anioMasAntiguo, int
anioMasReciente)` y un `EstadisticasService` que calcule el resumen de la colección:

```java
@Service
public class EstadisticasService {

    private final RepositorioLibros repositorio;

    public EstadisticasService(RepositorioLibros repositorio) {
        this.repositorio = repositorio;
    }

    public ResumenBiblioteca calcularResumen() {
        List<Libro> libros = repositorio.findAll();
        int total     = libros.size();
        int minAnio   = libros.stream().mapToInt(Libro::getAnio).min().orElse(0);
        int maxAnio   = libros.stream().mapToInt(Libro::getAnio).max().orElse(0);
        return new ResumenBiblioteca(total, minAnio, maxAnio);
    }
}
```

Añade un endpoint `GET /libros/resumen` en `LibroController` que devuelva el resumen.

1. ¿`LibroController` debe inyectar `EstadisticasService` directamente o debe pedírselo a
   `BibliotecaService`? Razona la respuesta.
2. ¿`RepositorioLibros` tiene ahora dos beans que lo inyectan (`BibliotecaService` y
   `EstadisticasService`)? ¿Es un problema para Spring?
3. Añade un libro nuevo con `@PostConstruct` y comprueba que el resumen refleja el
   cambio.

## Solución

`ResumenBiblioteca`, junto a `Libro` en `dominio/` (es información derivada de `Libro`,
no un DTO de respuesta HTTP como `LibroRespuesta` del Capítulo 2, así que encaja mejor
ahí que en `controlador/`):

```java
package com.javaesencial.biblioteca.dominio;

public record ResumenBiblioteca(int totalLibros, int anioMasAntiguo, int anioMasReciente) {
}
```

`EstadisticasService`, en `servicio/`:

```java
package com.javaesencial.biblioteca.servicio;

import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.dominio.ResumenBiblioteca;
import com.javaesencial.biblioteca.repositorio.RepositorioLibros;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstadisticasService {

    private final RepositorioLibros repositorio;

    public EstadisticasService(RepositorioLibros repositorio) {
        this.repositorio = repositorio;
    }

    public ResumenBiblioteca calcularResumen() {
        List<Libro> libros = repositorio.findAll();
        int total = libros.size();
        int minAnio = libros.stream().mapToInt(Libro::getAnio).min().orElse(0);
        int maxAnio = libros.stream().mapToInt(Libro::getAnio).max().orElse(0);
        return new ResumenBiblioteca(total, minAnio, maxAnio);
    }
}
```

### 1. ¿`LibroController` inyecta `EstadisticasService` directamente, o se lo pide a `BibliotecaService`?

Directamente. `EstadisticasService` no es un detalle interno de `BibliotecaService`: es
otro servicio de negocio, con su propia responsabilidad (calcular estadísticas, no
gestionar libros), que también depende de `RepositorioLibros` de forma independiente.
Obligar a `LibroController` a pasar por `BibliotecaService` para llegar a
`EstadisticasService` (por ejemplo, con un método `bibliotecaService.calcularResumen()`
que internamente delegara en `EstadisticasService`) acoplaría dos servicios que no tienen
ninguna relación real entre sí, solo para evitar que el controlador conozca a un segundo
servicio — y eso es justo lo que la inyección de dependencias permite evitar: un
controlador puede depender de tantos servicios como necesite, cada uno con su propia
responsabilidad, sin que eso sea un problema de diseño.

```java
@RestController
public class LibroController {

    private final BibliotecaService service;
    private final EstadisticasService estadisticasService;

    public LibroController(BibliotecaService service, EstadisticasService estadisticasService) {
        this.service = service;
        this.estadisticasService = estadisticasService;
    }

    @GetMapping("/libros")
    public List<Libro> listarTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/libros/buscar")
    public List<Libro> buscarPorAutor(@RequestParam(required = false) String autor) {
        return service.buscarPorAutor(autor);
    }

    @GetMapping("/libros/resumen")
    public ResumenBiblioteca resumen() {
        return estadisticasService.calcularResumen();
    }
}
```

### 2. `RepositorioLibros` inyectado por dos beans distintos

Sí, y **no es ningún problema para Spring**. `RepositorioLibros` sigue siendo un único
bean *singleton*: tanto `BibliotecaService` como `EstadisticasService` reciben, cada uno,
una referencia a esa misma instancia única por inyección de constructor. No se crean dos
`RepositorioLibros` distintos, uno por cada servicio que lo necesita — Spring resuelve la
dependencia con el mismo bean cada vez que alguien lo pide, tantas veces como haga falta.
Es exactamente el mismo mecanismo que ya se comprobó en la Práctica 3.1 con el campo
`"dependencies"` del endpoint `/actuator/beans`: ahora ese array de `repositorioLibros`
en la respuesta de `/actuator/beans` no cambia (sigue sin depender de nadie), pero si se
mirara el bean `estadisticasService`, su `"dependencies"` mostraría `["repositorioLibros"]`,
igual que `bibliotecaService`.

### 3. Añadir un libro con `@PostConstruct` y comprobar el resumen

`@PostConstruct` ya se usa en `RepositorioLibros.cargarDatosIniciales()` para cargar los
tres libros de partida; basta con añadir uno más a esa misma lista:

```java
@PostConstruct
public void cargarDatosIniciales() {
    libros.add(new Libro(1L, "El Quijote", "Miguel de Cervantes", 1605));
    libros.add(new Libro(2L, "1984", "George Orwell", 1949));
    libros.add(new Libro(3L, "Dune", "Frank Herbert", 1965));
    libros.add(new Libro(4L, "Cien años de soledad", "Gabriel García Márquez", 1967));
}
```

Con los cuatro libros cargados, `GET /libros/resumen` responde:

```json
{
  "totalLibros": 4,
  "anioMasAntiguo": 1605,
  "anioMasReciente": 1967
}
```

`totalLibros` pasa de 3 a 4; `anioMasAntiguo` sigue siendo 1605 (El Quijote, sin cambios);
`anioMasReciente` pasa de 1965 (Dune) a 1967 (Cien años de soledad), el nuevo libro más
reciente de la colección. Como `@PostConstruct` se ejecuta una sola vez, justo después de
que Spring construya `RepositorioLibros` al arrancar la aplicación, el libro nuevo forma
parte de los datos desde el primer momento — no hace falta ninguna petición adicional
para que aparezca.
