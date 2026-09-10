package com.javaesencial.biblioteca.controlador;

import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.servicio.BibliotecaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LibroController {

    private final BibliotecaService service;

    public LibroController(BibliotecaService service) {
        this.service = service;
    }

    @GetMapping("/libros")
    public List<Libro> listarTodos() {
        return service.obtenerTodos();
    }
}
