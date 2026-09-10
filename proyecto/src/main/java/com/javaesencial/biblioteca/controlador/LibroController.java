package com.javaesencial.biblioteca.controlador;

import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.servicio.BibliotecaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Libros", description = "Catálogo de la Biblioteca")
@RestController
@RequestMapping("/libros")
public class LibroController {

    private final BibliotecaService service;

    public LibroController(BibliotecaService service) {
        this.service = service;
    }

    @Operation(summary = "Lista el catálogo, paginado y con búsqueda opcional")
    @GetMapping
    public Page<Libro> listarTodos(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio,
            @RequestParam(defaultValue = "titulo") String orden,
            @RequestParam(defaultValue = "asc") String direccion,
            @RequestParam(required = false) String buscar) {

        Sort.Direction sentido = "desc".equalsIgnoreCase(direccion)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by(sentido, orden));
        return service.obtenerTodosPaginado(pageable, buscar);
    }

    @Operation(summary = "Obtiene un libro por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe ningún libro con ese id")
    })
    @GetMapping("/{id}")
    public Libro obtenerPorId(@Parameter(description = "Id del libro") @PathVariable Long id) {
        return service.buscarPorIdOFallar(id);
    }

    @Operation(summary = "Registra un libro nuevo")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Libro crear(@Valid @RequestBody Libro libro) {
        return service.registrar(libro);
    }

    @Operation(summary = "Actualiza un libro existente")
    @PutMapping("/{id}")
    public Libro actualizar(@PathVariable Long id, @Valid @RequestBody Libro libro) {
        return service.actualizar(id, libro);
    }

    @Operation(summary = "Elimina un libro")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
