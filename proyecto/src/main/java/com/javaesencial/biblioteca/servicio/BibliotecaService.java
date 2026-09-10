package com.javaesencial.biblioteca.servicio;

import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.dominio.LibroNoEncontradoException;
import com.javaesencial.biblioteca.repositorio.LibroJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Heredero declarado de {@code Biblioteca} (Volumen 1). Desde el
 * Capítulo 9 delega en {@code LibroJpaRepository} en vez del
 * repositorio en memoria.
 */
@Service
public class BibliotecaService {

    private static final Logger log = LoggerFactory.getLogger(BibliotecaService.class);

    private final LibroJpaRepository repositorio;

    public BibliotecaService(LibroJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Libro> obtenerTodos() {
        List<Libro> libros = repositorio.findAll();
        log.debug("Devolviendo {} libros del catálogo", libros.size());
        return libros;
    }

    public Page<Libro> obtenerTodosPaginado(Pageable pageable, String buscar) {
        if (buscar == null || buscar.isBlank()) {
            return repositorio.findAll(pageable);
        }
        return repositorio.buscarPorTextoPaginado(buscar, pageable);
    }

    public Optional<Libro> buscarPorId(Long id) {
        return repositorio.findById(id);
    }

    public Libro buscarPorIdOFallar(Long id) {
        return buscarPorId(id).orElseThrow(() -> new LibroNoEncontradoException(id));
    }

    public Libro registrar(Libro libro) {
        Libro creado = repositorio.save(libro);
        log.info("Libro registrado: {} (id={})", creado.getTitulo(), creado.getId());
        return creado;
    }

    public Libro actualizar(Long id, Libro datos) {
        Libro libro = buscarPorIdOFallar(id);
        libro.setTitulo(datos.getTitulo());
        libro.setAutor(datos.getAutor());
        libro.setAnio(datos.getAnio());
        return repositorio.save(libro);
    }

    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new LibroNoEncontradoException(id);
        }
        repositorio.deleteById(id);
    }
}
