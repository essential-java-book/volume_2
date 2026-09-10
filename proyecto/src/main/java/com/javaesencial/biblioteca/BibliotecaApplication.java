package com.javaesencial.biblioteca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Spring Boot.
 *
 * Sustituye a {@code ui.Main} del Volumen 1: en vez de un menú de consola,
 * arranca un servidor HTTP embebido (Tomcat) en el puerto 8080.
 */
@SpringBootApplication
public class BibliotecaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibliotecaApplication.class, args);
    }
}
