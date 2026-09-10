package com.javaesencial.biblioteca.configuracion;

import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.repositorio.RepositorioLibros;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Datos de prueba del catálogo, solo activos con el perfil {@code dev}.
 * En producción el repositorio nace vacío (aquí, temporalmente: la
 * persistencia real llega en el Capítulo 9).
 */
@Component
@Profile("dev")
public class CargadorDatosPrueba {

    private final RepositorioLibros repositorio;

    public CargadorDatosPrueba(RepositorioLibros repositorio) {
        this.repositorio = repositorio;
    }

    @PostConstruct
    public void cargar() {
        repositorio.agregar(new Libro(null, "El Quijote", "Miguel de Cervantes", 1605));
        repositorio.agregar(new Libro(null, "1984", "George Orwell", 1949));
        repositorio.agregar(new Libro(null, "Dune", "Frank Herbert", 1965));
    }
}
