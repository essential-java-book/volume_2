package com.javaesencial.biblioteca.controlador;

import com.javaesencial.biblioteca.configuracion.BibliotecaConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

    private final BibliotecaConfig config;

    public InfoController(BibliotecaConfig config) {
        this.config = config;
    }

    @GetMapping("/info")
    public BibliotecaConfig info() {
        return config;
    }
}
