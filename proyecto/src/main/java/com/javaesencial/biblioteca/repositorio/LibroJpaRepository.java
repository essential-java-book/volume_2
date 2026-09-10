package com.javaesencial.biblioteca.repositorio;

import com.javaesencial.biblioteca.dominio.Libro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data JPA genera la implementación (findAll, findById, save,
 * deleteById, existsById...) a partir de esta interfaz. Sustituye a
 * {@code RepositorioLibros} (en memoria, Capítulos 3-8).
 *
 * Desde el Capítulo 11, {@code findAll(Pageable)} llega gratis del
 * propio {@code JpaRepository}; la búsqueda por texto es la única
 * consulta que hay que escribir a mano.
 */
public interface LibroJpaRepository extends JpaRepository<Libro, Long> {

    Page<Libro> findByTituloIgnoreCase(String titulo, Pageable pageable);

    Page<Libro> findByAutorIgnoreCase(String autor, Pageable pageable);

    @Query("""
            SELECT l FROM Libro l
            WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(l.autor) LIKE LOWER(CONCAT('%', :texto, '%'))
            """)
    Page<Libro> buscarPorTextoPaginado(String texto, Pageable pageable);
}
