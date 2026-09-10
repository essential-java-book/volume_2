package com.javaesencial.biblioteca.repositorio;

import com.javaesencial.biblioteca.dominio.Libro;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio en memoria -- heredero declarado de {@code Repositorio<T>}
 * del Volumen 1, ahora gestionado por el contenedor IoC. El Capítulo 9
 * lo sustituye por {@code LibroJpaRepository}.
 *
 * Desde el Capítulo 4 ya no carga datos por sí mismo: los datos de
 * prueba los añade {@code CargadorDatosPrueba}, solo en el perfil
 * {@code dev} (así el repositorio nace vacío en producción).
 */
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
