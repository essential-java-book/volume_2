package com.javaesencial.biblioteca.repositorio;

import com.javaesencial.biblioteca.dominio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpaRepository extends JpaRepository<Usuario, Long> {
}
