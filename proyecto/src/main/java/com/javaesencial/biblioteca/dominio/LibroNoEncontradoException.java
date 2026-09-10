package com.javaesencial.biblioteca.dominio;

/**
 * Se lanza cuando se pide, modifica o borra un libro cuyo id no existe.
 * {@code ManejadorGlobalErrores} la traduce a un 404 con {@code ProblemDetail}.
 */
public class LibroNoEncontradoException extends RuntimeException {

    public LibroNoEncontradoException(Long id) {
        super("No existe ningún libro con id " + id);
    }
}
