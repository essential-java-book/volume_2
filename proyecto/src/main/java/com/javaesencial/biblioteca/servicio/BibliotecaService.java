package com.javaesencial.biblioteca.servicio;

import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.repositorio.RepositorioLibros;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Heredero declarado de {@code Biblioteca} (Volumen 1): concentra la
 * lógica de negocio y delega la persistencia en el repositorio que
 * Spring le inyecta por constructor.
 */
@Service
public class BibliotecaService {

    private final RepositorioLibros repositorio;

    public BibliotecaService(RepositorioLibros repositorio) {
        this.repositorio = repositorio;
    }

    public List<Libro> obtenerTodos() {
        return repositorio.findAll();
    }

    public Optional<Libro> buscarPorId(Long id) {
        return repositorio.findById(id);
    }
}
