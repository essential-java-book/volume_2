package com.javaesencial.biblioteca.dominio;

/**
 * Versión reducida del {@code Libro} del Volumen 1: solo lo necesario
 * para aprender Spring (sin precio, stock ni género). El ISBN vuelve
 * en el Capítulo 12; hasta entonces, id + título + autor + año.
 */
public class Libro {

    private Long id;
    private String titulo;
    private String autor;
    private int anio;

    public Libro() {
    }

    public Libro(Long id, String titulo, String autor, int anio) {
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

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }
}
