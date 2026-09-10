package com.javaesencial.biblioteca.dominio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Versión reducida del {@code Libro} del Volumen 1: solo lo necesario
 * para aprender Spring (sin precio, stock ni género). El ISBN vuelve
 * en el Capítulo 12; hasta entonces, id + título + autor + año.
 *
 * Desde el Capítulo 6, validado con Bean Validation: {@code anio} pasa
 * de {@code int} a {@code Integer} para poder anotarlo con
 * {@code @NotNull} (un {@code int} nunca es nulo).
 */
public class Libro {

    private Long id;

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @NotBlank(message = "El autor es obligatorio")
    private String autor;

    @NotNull(message = "El año es obligatorio")
    private Integer anio;

    public Libro() {
    }

    public Libro(Long id, String titulo, String autor, Integer anio) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.anio = anio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }
}
