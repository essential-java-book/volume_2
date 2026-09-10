package com.javaesencial.biblioteca.configuracion;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Agrupa las propiedades propias de la aplicación bajo el prefijo
 * {@code biblioteca.*} en un objeto tipado, en vez de leerlas una a
 * una con {@code @Value}.
 */
@Component
@ConfigurationProperties(prefix = "biblioteca")
public class BibliotecaConfig {

    private String nombre;
    private String version;
    private int maxLibros;
    private boolean modoMantenimiento;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getMaxLibros() {
        return maxLibros;
    }

    public void setMaxLibros(int maxLibros) {
        this.maxLibros = maxLibros;
    }

    public boolean isModoMantenimiento() {
        return modoMantenimiento;
    }

    public void setModoMantenimiento(boolean modoMantenimiento) {
        this.modoMantenimiento = modoMantenimiento;
    }
}
