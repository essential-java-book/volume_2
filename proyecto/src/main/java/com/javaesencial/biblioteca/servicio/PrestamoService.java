package com.javaesencial.biblioteca.servicio;

import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.dominio.LibroNoEncontradoException;
import com.javaesencial.biblioteca.dominio.Prestamo;
import com.javaesencial.biblioteca.dominio.PrestamoNoEncontradoException;
import com.javaesencial.biblioteca.dominio.Usuario;
import com.javaesencial.biblioteca.dominio.UsuarioNoEncontradoException;
import com.javaesencial.biblioteca.repositorio.LibroJpaRepository;
import com.javaesencial.biblioteca.repositorio.PrestamoJpaRepository;
import com.javaesencial.biblioteca.repositorio.UsuarioJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PrestamoService {

    private final PrestamoJpaRepository prestamos;
    private final UsuarioJpaRepository usuarios;
    private final LibroJpaRepository libros;

    public PrestamoService(PrestamoJpaRepository prestamos,
                            UsuarioJpaRepository usuarios,
                            LibroJpaRepository libros) {
        this.prestamos = prestamos;
        this.usuarios = usuarios;
        this.libros = libros;
    }

    @Transactional(readOnly = true)
    public List<Prestamo> obtenerTodos() {
        return prestamos.buscarTodosConUsuarioYLibro();
    }

    @Transactional(readOnly = true)
    public List<Prestamo> obtenerPorUsuario(Long usuarioId) {
        if (!usuarios.existsById(usuarioId)) {
            throw new UsuarioNoEncontradoException(usuarioId);
        }
        return prestamos.buscarPorUsuarioConLibro(usuarioId);
    }

    public Prestamo registrar(Long usuarioId, Long libroId) {
        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));
        Libro libro = libros.findById(libroId)
                .orElseThrow(() -> new LibroNoEncontradoException(libroId));

        Prestamo prestamo = new Prestamo(usuario, libro, LocalDate.now());
        return prestamos.save(prestamo);
    }

    public Prestamo registrarDevolucion(Long id) {
        Prestamo prestamo = prestamos.buscarPorIdConUsuarioYLibro(id)
                .orElseThrow(() -> new PrestamoNoEncontradoException(id));
        prestamo.setFechaDevolucion(LocalDate.now());
        return prestamos.save(prestamo);
    }
}
