# Práctica 4.4 — Añadir SLF4J a `BibliotecaService`

## Enunciado

Añade un logger a `BibliotecaService` y registra una línea en cada operación:

```java
private static final Logger log = LoggerFactory.getLogger(BibliotecaService.class);
```

Usa `log.debug(...)` para las operaciones de lectura y `log.info(...)` para las de
escritura y eliminación.

1. Con el perfil `dev` (nivel `DEBUG`), ¿qué líneas aparecen en la consola al llamar a
   `GET /libros`?
2. Con el perfil `prod` (nivel `WARN`), ¿aparece alguna línea del servicio?
3. ¿Por qué se usa `log.debug("Solicitando libros")` en lugar de
   `System.out.println("Solicitando libros")`?

## Solución

`BibliotecaService` con el logger añadido (`BibliotecaService` de este volumen solo tiene
operaciones de lectura hasta el momento — `obtenerTodos()` y `buscarPorId()` —, así que
ambas van con `log.debug`, tal como pide el enunciado para las operaciones de lectura; las
de escritura/eliminación con `log.info` llegarán cuando el capítulo correspondiente las
añada):

```java
package com.javaesencial.biblioteca.servicio;

import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.repositorio.RepositorioLibros;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BibliotecaService {

    private static final Logger log = LoggerFactory.getLogger(BibliotecaService.class);

    private final RepositorioLibros repositorio;

    public BibliotecaService(RepositorioLibros repositorio) {
        this.repositorio = repositorio;
    }

    public List<Libro> obtenerTodos() {
        log.debug("Solicitando todos los libros");
        return repositorio.findAll();
    }

    public Optional<Libro> buscarPorId(Long id) {
        log.debug("Buscando libro con id {}", id);
        return repositorio.findById(id);
    }
}
```

### 1. Con `dev` (nivel `DEBUG`) y `GET /libros`

```terminal
DEBUG 8421 --- [nio-8080-exec-1] c.j.b.servicio.BibliotecaService : Solicitando todos los libros
```

La línea aparece: `application-dev.properties` fija
`logging.level.com.javaesencial.biblioteca=DEBUG`, y `DEBUG` es exactamente el nivel al
que se ha registrado este mensaje —`DEBUG` está habilitado, así que se muestra—.

### 2. Con `prod` (nivel `WARN`)

**No aparece ninguna línea** de `BibliotecaService`. Los niveles de log tienen un orden
de severidad (de menor a mayor): `TRACE < DEBUG < INFO < WARN < ERROR`. Cuando el nivel
configurado es `WARN`, solo se muestran los mensajes de `WARN` o más graves —`ERROR`—;
todo lo registrado con `debug(...)` o `info(...)` queda silenciado, aunque la línea de
código que lo genera se siga ejecutando exactamente igual (el coste de formatear el
mensaje existe siempre; lo que cambia es si se escribe a la salida). Como
`log.debug("Solicitando todos los libros")` está por debajo de `WARN`, sencillamente no
se imprime.

### 3. `log.debug(...)` frente a `System.out.println(...)`

`System.out.println` siempre escribe, en cualquier entorno, sin forma de desactivarlo sin
tocar el código ni de distinguir "esto es información de diagnóstico" de "esto es un
mensaje importante". Con SLF4J/Logback en cambio: el nivel se controla desde fuera del
código (`application-dev.properties` frente a `application-prod.properties`, como se
acaba de comprobar), así que la misma línea de código sirve para desarrollo (donde
interesa ver el detalle) y para producción (donde ese mismo detalle sería ruido) sin
cambiar el código ni recompilar; cada línea queda automáticamente etiquetada con fecha,
nivel, hilo y la clase que la emitió (visible en el propio formato de la línea de arriba),
algo que `System.out.println` no da gratis; y, con formato de plantilla
(`"Buscando libro con id {}", id`), el mensaje solo se construye realmente si el nivel
está activo, evitando el coste de concatenar cadenas cuando ese log ni siquiera se va a
mostrar. Es el mismo argumento de fondo que "externalizar en vez de fijar en el código"
que ya apareció en la Práctica 2.1 (el puerto) y en la 4.1 (el mensaje de bienvenida):
separar la decisión ("¿cuánto detalle quiero ver?") del código que la genera.
