package com.javaesencial.biblioteca.dominio;

public class PrestamoNoEncontradoException extends RuntimeException {

    public PrestamoNoEncontradoException(Long id) {
        super("No existe ningún préstamo con id " + id);
    }
}
