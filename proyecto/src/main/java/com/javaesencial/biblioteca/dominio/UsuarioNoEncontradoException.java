package com.javaesencial.biblioteca.dominio;

public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(Long id) {
        super("No existe ningún usuario con id " + id);
    }
}
