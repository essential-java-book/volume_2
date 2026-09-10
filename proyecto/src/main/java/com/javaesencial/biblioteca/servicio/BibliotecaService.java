package com.javaesencial.biblioteca.servicio;

import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.dominio.LibroNoEncontradoException;
import com.javaesencial.biblioteca.repositorio.RepositorioLibros;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(BibliotecaService.class);

    private final RepositorioLibros repositorio;

    public BibliotecaService(RepositorioLibros repositorio) {
        this.repositorio = repositorio;
    }

    public List<Libro> obtenerTodos() {
        List<Libro> libros = repositorio.findAll();
        log.debug("Devolviendo {} libros del catálogo", libros.size());
        return libros;
    }

    public Optional<Libro> buscarPorId(Long id) {
        return repositorio.findById(id);
    }

    public Libro buscarPorIdOFallar(Long id) {
        return buscarPorId(id).orElseThrow(() -> new LibroNoEncontradoException(id));
    }

    public Libro registrar(Libro libro) {
        Libro creado = repositorio.agregar(libro);
        log.info("Libro registrado: {} (id={})", creado.getTitulo(), creado.getId());
        return creado;
    }

    public Libro actualizar(Long id, Libro datos) {
        return repositorio.actualizar(id, datos)
                .orElseThrow(() -> new LibroNoEncontradoException(id));
    }

    public void eliminar(Long id) {
        if (!repositorio.eliminar(id)) {
            throw new LibroNoEncontradoException(id);
        }
    }
}
