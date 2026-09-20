# Práctica 3.2 — Singleton en acción

## Enunciado

Añade un contador de peticiones a `BibliotecaService` para demostrar que es un singleton:

```java
private int contadorPeticiones = 0;

public List<Libro> obtenerTodos() {
    contadorPeticiones++;
    System.out.println("Petición número: " + contadorPeticiones);
    return repositorio.findAll();
}
```

Realiza cinco peticiones a `GET /libros` desde `pruebas.http` y observa la consola.

1. ¿El contador se incrementa de forma acumulada o reinicia en cada petición?
2. ¿Qué ocurriría si `BibliotecaService` tuviese ámbito `prototype` en lugar de
   `singleton`?
3. ¿Tiene sentido que un `@Service` sea `prototype`? ¿En qué casos podría serlo?

## Solución

`BibliotecaService` con el contador añadido:

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
    private int contadorPeticiones = 0;

    public BibliotecaService(RepositorioLibros repositorio) {
        this.repositorio = repositorio;
    }

    public List<Libro> obtenerTodos() {
        contadorPeticiones++;
        System.out.println("Petición número: " + contadorPeticiones);
        return repositorio.findAll();
    }

    public Optional<Libro> buscarPorId(Long id) {
        return repositorio.findById(id);
    }
}
```

### 1. Cinco peticiones a `GET /libros`

```terminal
Petición número: 1
Petición número: 2
Petición número: 3
Petición número: 4
Petición número: 5
```

El contador **se acumula**, nunca reinicia. Cada petición HTTP es independiente (el
servidor no "recuerda" nada de una a otra por sí mismo), pero `LibroController` —también
singleton— mantiene siempre la misma referencia al mismo objeto `BibliotecaService`, así
que `contadorPeticiones` es el mismo campo, en la misma instancia, incrementándose cada
vez. Si Spring creara un `BibliotecaService` nuevo por cada petición, el contador
volvería a `1` en cada una — eso es justo lo que esta práctica demuestra que *no* ocurre.

### 2. Si `BibliotecaService` fuera `prototype`

Aquí hay un matiz importante, más allá de la respuesta ingenua ("cada petición tendría su
propio contador"). `LibroController` es un *singleton*, y recibe su `BibliotecaService`
por **inyección de constructor, una sola vez**, cuando el propio `LibroController` se
crea (al arrancar la aplicación). Aunque `BibliotecaService` estuviera anotado con
`@Scope("prototype")`, Spring solo pediría una instancia *prototype* nueva **en el
momento de construir `LibroController`** — y esa misma instancia quedaría guardada en el
campo `service` de `LibroController` para siempre, reutilizándose en todas las
peticiones exactamente igual que si fuera singleton. Es un error común: pensar que un
bean `prototype` inyectado en un singleton se renueva en cada uso, cuando en realidad la
inyección directa por constructor "congela" esa única instancia *prototype* dentro del
singleton que la recibió. (Conseguir una instancia *prototype* nueva en cada petición
exigiría un mecanismo adicional, como un `ObjectProvider<BibliotecaService>` o un *scoped
proxy*, que quedan fuera del alcance de este capítulo.)

### 3. ¿Tiene sentido un `@Service` `prototype`?

En la inmensa mayoría de los casos, no: un servicio sin estado propio (como
`BibliotecaService` sin el contador) no gana nada con `prototype` — crear una instancia
nueva cada vez es puro coste sin beneficio, y por eso `singleton` es el ámbito por
defecto y el que se usa durante todo este volumen. `prototype` empieza a tener sentido
cuando el bean necesita guardar **estado mutable propio de una única operación o de un
único cliente** que no debe compartirse entre usos concurrentes —por ejemplo, un objeto
que acumula pasos de un asistente de varias fases, o un constructor de consultas
complejas que va añadiendo condiciones antes de ejecutarse una sola vez—; en esos casos,
compartir una única instancia *singleton* mezclaría el estado de peticiones distintas de
forma insegura. No es el caso de `BibliotecaService`: aquí, precisamente, lo que la
práctica demuestra es que compartir el contador entre peticiones es el comportamiento
correcto y esperado.
