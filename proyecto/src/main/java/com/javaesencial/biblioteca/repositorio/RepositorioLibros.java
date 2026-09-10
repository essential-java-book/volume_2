package com.javaesencial.biblioteca.repositorio;

import com.javaesencial.biblioteca.dominio.Libro;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repositorio en memoria -- heredero declarado de {@code Repositorio<T>}
 * del Volumen 1, ahora gestionado por el contenedor IoC. El Capítulo 9
 * lo sustituye por {@code LibroJpaRepository}.
 *
 * Desde el Capítulo 4 ya no carga datos por sí mismo: los datos de
 * prueba los añade {@code CargadorDatosPrueba}, solo en el perfil
 * {@code dev}. Desde el Capítulo 5 soporta el CRUD completo.
 */
@Repository
public class RepositorioLibros {

    private final List<Libro> libros = new ArrayList<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    public List<Libro> findAll() {
        return libros;
    }

    public Optional<Libro> findById(Long id) {
        return libros.stream()
                .filter(libro -> libro.getId().equals(id))
                .findFirst();
    }

    public Libro agregar(Libro libro) {
        libro.setId(secuencia.incrementAndGet());
        libros.add(libro);
        return libro;
    }

    public Optional<Libro> actualizar(Long id, Libro datos) {
        return findById(id).map(libro -> {
            libro.setTitulo(datos.getTitulo());
            libro.setAutor(datos.getAutor());
            libro.setAnio(datos.getAnio());
            return libro;
        });
    }

    public boolean eliminar(Long id) {
        return libros.removeIf(libro -> libro.getId().equals(id));
    }
}
