package com.javaesencial.biblioteca.seguridad;

public record RespuestaToken(String token, String tipo) {

    public RespuestaToken(String token) {
        this(token, "Bearer");
    }
}
