package com.javaesencial.biblioteca.controlador;

/**
 * DTO de respuesta para el endpoint de prueba {@code /libro-ejemplo}.
 * Un {@code record} genera constructor, getters, {@code equals},
 * {@code hashCode} y {@code toString} sin escribir una línea.
 */
public record LibroRespuesta(Long id, String titulo, String autor, int anio) {
}
