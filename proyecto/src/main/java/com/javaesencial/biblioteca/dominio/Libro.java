package com.javaesencial.biblioteca.dominio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Versión reducida del {@code Libro} del Volumen 1: solo lo necesario
 * para aprender Spring (sin precio, stock ni género). El ISBN vuelve
 * en el Capítulo 12; hasta entonces, id + título + autor + año.
 *
 * Desde el Capítulo 9, entidad JPA: Hibernate genera la tabla
 * {@code libro} y {@code LibroJpaRepository} sustituye a
 * {@code RepositorioLibros}.
 */
@Schema(description = "Un libro del catálogo de la Biblioteca Municipal \"El Quijote\"")
@Entity
@Table(name = "libro")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único", example = "1")
    private Long id;

    @Schema(description = "Título del libro", example = "El Quijote")
    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @Schema(description = "Autor del libro", example = "Miguel de Cervantes")
    @NotBlank(message = "El autor es obligatorio")
    private String autor;

    @Schema(description = "Año de publicación", example = "1605")
    @NotNull(message = "El año es obligatorio")
    private Integer anio;

    @Schema(description = "ISBN (opcional, vuelve al catálogo en el Capítulo 12)", example = "978-84-376-0494-7")
    private String isbn;

    public Libro() {
    }

    public Libro(Long id, String titulo, String autor, Integer anio) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.anio = anio;
    }

    public Libro(Long id, String titulo, String autor, Integer anio, String isbn) {
        this(id, titulo, autor, anio);
        this.isbn = isbn;
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

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
