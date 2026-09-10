package com.javaesencial.biblioteca.repositorio;

import com.javaesencial.biblioteca.dominio.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA genera la implementación (findAll, findById, save,
 * deleteById, existsById...) a partir de esta interfaz. Sustituye a
 * {@code RepositorioLibros} (en memoria, Capítulos 3-8).
 */
public interface LibroJpaRepository extends JpaRepository<Libro, Long> {
}
