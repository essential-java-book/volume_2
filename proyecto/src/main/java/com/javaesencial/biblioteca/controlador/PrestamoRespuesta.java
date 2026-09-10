package com.javaesencial.biblioteca.controlador;

import com.javaesencial.biblioteca.dominio.Prestamo;

import java.time.LocalDate;

/**
 * DTO de salida para {@code Prestamo}: aplana la relación con
 * {@code Usuario} y {@code Libro} en campos sueltos, así se evita la
 * serialización de proxies LAZY (y una futura referencia circular si
 * algún día {@code Usuario}/{@code Libro} listan sus préstamos).
 */
public record PrestamoRespuesta(
        Long id,
        Long usuarioId,
        String nombreUsuario,
        Long libroId,
        String tituloLibro,
        LocalDate fechaPrestamo,
        LocalDate fechaDevolucion) {

    public static PrestamoRespuesta desde(Prestamo prestamo) {
        return new PrestamoRespuesta(
                prestamo.getId(),
                prestamo.getUsuario().getId(),
                prestamo.getUsuario().getNombre(),
                prestamo.getLibro().getId(),
                prestamo.getLibro().getTitulo(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaDevolucion());
    }
}
