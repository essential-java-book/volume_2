package com.javaesencial.biblioteca;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Primer controlador del volumen: dos endpoints de prueba para comprobar
 * que el servidor arranca y que Spring serializa JSON automáticamente.
 */
@RestController
public class HolaMundoController {

    @GetMapping("/hola")
    public String hola() {
        return "Hola desde la Biblioteca Municipal \"El Quijote\"";
    }

    @GetMapping("/libro-ejemplo")
    public LibroRespuesta libroEjemplo() {
        return new LibroRespuesta(1L, "El Quijote", "Miguel de Cervantes", 1605);
    }
}
