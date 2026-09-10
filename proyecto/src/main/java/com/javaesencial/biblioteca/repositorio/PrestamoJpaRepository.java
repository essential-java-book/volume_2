package com.javaesencial.biblioteca.repositorio;

import com.javaesencial.biblioteca.dominio.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Las tres consultas usan {@code JOIN FETCH} para traer {@code usuario}
 * y {@code libro} en la misma consulta: evita el problema N+1 y evita
 * el {@code LazyInitializationException} al mapear a
 * {@code PrestamoRespuesta} fuera de la transacción.
 */
public interface PrestamoJpaRepository extends JpaRepository<Prestamo, Long> {

    @Query("SELECT p FROM Prestamo p JOIN FETCH p.usuario JOIN FETCH p.libro")
    List<Prestamo> buscarTodosConUsuarioYLibro();

    @Query("SELECT p FROM Prestamo p JOIN FETCH p.usuario JOIN FETCH p.libro WHERE p.usuario.id = :usuarioId")
    List<Prestamo> buscarPorUsuarioConLibro(Long usuarioId);

    @Query("SELECT p FROM Prestamo p JOIN FETCH p.usuario JOIN FETCH p.libro WHERE p.id = :id")
    Optional<Prestamo> buscarPorIdConUsuarioYLibro(Long id);
}
