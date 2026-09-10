package com.javaesencial.biblioteca.controlador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.dominio.LibroNoEncontradoException;
import com.javaesencial.biblioteca.servicio.BibliotecaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Suite de tests de la capa REST de {@code LibroController}, aislada
 * del resto de la aplicación con {@code @WebMvcTest}: solo se carga
 * el controlador y el contexto MVC; {@code BibliotecaService} se
 * sustituye por un doble con {@code @MockBean}.
 */
@WebMvcTest(LibroController.class)
class LibroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BibliotecaService service;

    @Test
    void listarTodosDevuelveElCatalogoPaginado() throws Exception {
        Libro elQuijote = new Libro(1L, "El Quijote", "Miguel de Cervantes", 1605);
        Page<Libro> pagina = new PageImpl<>(List.of(elQuijote));
        when(service.obtenerTodosPaginado(any(), isNull())).thenReturn(pagina);

        mockMvc.perform(get("/libros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("El Quijote"));
    }

    @Test
    void obtenerPorIdDevuelveElLibroSiExiste() throws Exception {
        Libro libro = new Libro(1L, "El Quijote", "Miguel de Cervantes", 1605);
        when(service.buscarPorIdOFallar(1L)).thenReturn(libro);

        mockMvc.perform(get("/libros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("El Quijote"));
    }

    @Test
    void obtenerPorIdDevuelve404SiNoExiste() throws Exception {
        when(service.buscarPorIdOFallar(99L)).thenThrow(new LibroNoEncontradoException(99L));

        mockMvc.perform(get("/libros/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Libro no encontrado"));
    }

    @Test
    void crearRegistraUnLibroNuevo() throws Exception {
        Libro entrada = new Libro(null, "Dune", "Frank Herbert", 1965);
        Libro creado = new Libro(4L, "Dune", "Frank Herbert", 1965);
        when(service.registrar(any(Libro.class))).thenReturn(creado);

        mockMvc.perform(post("/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.titulo").value("Dune"));
    }

    @Test
    void crearRechazaUnLibroSinTitulo() throws Exception {
        Libro sinTitulo = new Libro(null, "", "Frank Herbert", 1965);

        mockMvc.perform(post("/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sinTitulo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Datos de entrada no válidos"));

        verify(service, never()).registrar(any());
    }

    @Test
    void crearRechazaUnLibroSinAnio() throws Exception {
        String json = "{\"titulo\":\"Dune\",\"autor\":\"Frank Herbert\"}";

        mockMvc.perform(post("/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarModificaUnLibroExistente() throws Exception {
        Libro cambios = new Libro(null, "El Quijote (edición revisada)", "Miguel de Cervantes", 1605);
        Libro actualizado = new Libro(1L, "El Quijote (edición revisada)", "Miguel de Cervantes", 1605);
        when(service.actualizar(eq(1L), any(Libro.class))).thenReturn(actualizado);

        mockMvc.perform(put("/libros/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambios)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("El Quijote (edición revisada)"));
    }

    @Test
    void actualizarDevuelve404SiNoExiste() throws Exception {
        Libro cambios = new Libro(null, "No existe", "Nadie", 2000);
        when(service.actualizar(eq(99L), any(Libro.class))).thenThrow(new LibroNoEncontradoException(99L));

        mockMvc.perform(put("/libros/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambios)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarBorraUnLibroExistente() throws Exception {
        mockMvc.perform(delete("/libros/1"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(1L);
    }

    @Test
    void eliminarDevuelve404SiNoExiste() throws Exception {
        doThrow(new LibroNoEncontradoException(99L)).when(service).eliminar(99L);

        mockMvc.perform(delete("/libros/99"))
                .andExpect(status().isNotFound());
    }
}
