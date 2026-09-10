package com.javaesencial.biblioteca.repositorio;

import com.javaesencial.biblioteca.dominio.Credencial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredencialJpaRepository extends JpaRepository<Credencial, Long> {

    Optional<Credencial> findByUsername(String username);
}
