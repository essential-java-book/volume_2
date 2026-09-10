package com.javaesencial.biblioteca.controlador;

import com.javaesencial.biblioteca.servicio.PrestamoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Préstamos", description = "Préstamos de libros a socios")
@RestController
@RequestMapping("/prestamos")
public class PrestamoController {

    private final PrestamoService service;

    public PrestamoController(PrestamoService service) {
        this.service = service;
    }

    @GetMapping
    public List<PrestamoRespuesta> listarTodos() {
        return service.obtenerTodos().stream().map(PrestamoRespuesta::desde).toList();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<PrestamoRespuesta> listarPorUsuario(@PathVariable Long usuarioId) {
        return service.obtenerPorUsuario(usuarioId).stream().map(PrestamoRespuesta::desde).toList();
    }

    @PostMapping("/usuario/{usuarioId}/libro/{libroId}")
    @ResponseStatus(HttpStatus.CREATED)
    public PrestamoRespuesta registrar(@PathVariable Long usuarioId, @PathVariable Long libroId) {
        return PrestamoRespuesta.desde(service.registrar(usuarioId, libroId));
    }

    @PatchMapping("/{id}/devolucion")
    public PrestamoRespuesta registrarDevolucion(@PathVariable Long id) {
        return PrestamoRespuesta.desde(service.registrarDevolucion(id));
    }
}
