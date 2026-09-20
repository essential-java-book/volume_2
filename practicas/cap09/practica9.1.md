# Práctica 9.1 — Método de búsqueda por autor

## Enunciado

Añade en `LibroJpaRepository` el método derivado `findByAutorIgnoreCase(String autor)`
que devuelva todos los libros cuyo autor coincida con el nombre indicado (sin distinguir
mayúsculas de minúsculas). Añade en `BibliotecaService` el método
`buscarPorAutor(String autor)` que delegue en el repositorio. No hace falta modificar el
controlador en esta práctica.

## Solución

`LibroJpaRepository` con el método derivado añadido:

```java
package com.javaesencial.biblioteca.repositorio;

import com.javaesencial.biblioteca.dominio.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibroJpaRepository extends JpaRepository<Libro, Long> {

    List<Libro> findByAutorIgnoreCase(String autor);
}
```

`BibliotecaService` con el método nuevo delegando:

```java
public List<Libro> buscarPorAutor(String autor) {
    return repositorio.findByAutorIgnoreCase(autor);
}
```

### Cómo genera Spring Data la consulta

No hace falta escribir ninguna implementación: Spring Data JPA analiza el **nombre del
método** en tiempo de arranque y construye la consulta a partir de esa lectura. `findBy`
indica que es una búsqueda; `Autor` identifica el campo `autor` de la entidad `Libro`
(coincide exactamente con el nombre del atributo, respetando mayúsculas/minúsculas de
`camelCase`); `IgnoreCase` añade la comparación insensible a mayúsculas. El resultado
equivale a esta consulta JPQL, generada automáticamente:

```
SELECT l FROM Libro l WHERE LOWER(l.autor) = LOWER(:autor)
```

Si el nombre del método no coincidiera con ningún campo real de `Libro` (por ejemplo,
`findByAutorr`, con una errata), la aplicación fallaría **al arrancar**, no en tiempo de
ejecución: Spring Data valida que cada método derivado tenga sentido contra la entidad en
el momento de construir el contexto, antes de que la aplicación llegue a aceptar ninguna
petición.

### Por qué no hace falta tocar el controlador

El enunciado lo deja explícito a propósito: esta práctica solo construye las dos capas
inferiores (repositorio y servicio). Es el mismo criterio de "capas independientes" que
ya se razonó en la Práctica 3.3 y en la 5.3 — el método queda listo y probable de forma
aislada (por ejemplo, con un test de servicio al estilo de la Práctica 8.4) antes de
decidir si, cómo y con qué ruta se expone al exterior; eso es precisamente lo que hace la
siguiente práctica, la 9.2.
