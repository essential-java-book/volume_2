package com.javaesencial.biblioteca.repositorio;

import com.javaesencial.biblioteca.dominio.Libro;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio en memoria -- heredero declarado de {@code Repositorio<T>}
 * del Volumen 1, ahora gestionado por el contenedor IoC. El Capítulo 9
 * lo sustituye por {@code LibroJpaRepository}.
 */
@Repository
public class RepositorioLibros {

    private final List<Libro> libros = new ArrayList<>();

    @PostConstruct
    public void cargarDatosIniciales() {
        libros.add(new Libro(1L, "El Quijote", "Miguel de Cervantes", 1605));
        libros.add(new Libro(2L, "1984", "George Orwell", 1949));
        libros.add(new Libro(3L, "Dune", "Frank Herbert", 1965));
    }

    public List<Libro> findAll() {
        return libros;
    }

    public Optional<Libro> findById(Long id) {
        return libros.stream()
                .filter(libro -> libro.getId().equals(id))
                .findFirst();
    }
}
