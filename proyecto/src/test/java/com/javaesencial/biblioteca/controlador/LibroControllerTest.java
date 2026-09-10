package com.javaesencial.biblioteca.controlador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaesencial.biblioteca.configuracion.SeguridadConfig;
import com.javaesencial.biblioteca.dominio.Libro;
import com.javaesencial.biblioteca.dominio.LibroNoEncontradoException;
import com.javaesencial.biblioteca.seguridad.BibliotecaUserDetailsService;
import com.javaesencial.biblioteca.seguridad.JwtFiltro;
import com.javaesencial.biblioteca.seguridad.JwtServicio;
import com.javaesencial.biblioteca.servicio.BibliotecaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Suite de tests de la capa REST de {@code LibroController}, aislada
 * del resto de la aplicación con {@code @WebMvcTest}. Desde el
 * Capítulo 13 hay que importar {@code SeguridadConfig} explícitamente
 * (no forma parte del slice web por defecto) y autenticar con
 * {@code @WithMockUser} las peticiones que ya no son públicas. Desde
 * el Capítulo 14, {@code SeguridadConfig} exige un {@code JwtFiltro}
 * como colaborador, así que también se importa y se sustituye
 * {@code JwtServicio} por un mock (los tests siguen autenticando con
 * {@code @WithMockUser}, no con tokens reales).
 */
@WebMvcTest(LibroController.class)
@Import({SeguridadConfig.class, JwtFiltro.class})
class LibroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BibliotecaService service;

    @MockBean
    private BibliotecaUserDetailsService userDetailsService;

    @MockBean
    private JwtServicio jwtServicio;

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
    @WithMockUser(roles = "ADMIN")
    void crearRegistraUnLibroNuevo() throws Exception {
        Libro entrada = new Libro(null, "Dune", "Frank Herbert", 1965);
        Libro creado = new Libro(4L, "Dune", "Frank Herbert", 1965);
        when(service.registrar(any(Libro.class))).thenReturn(creado);

        mockMvc.perform(post("/libros")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.titulo").value("Dune"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crearRechazaUnLibroSinTitulo() throws Exception {
        Libro sinTitulo = new Libro(null, "", "Frank Herbert", 1965);

        mockMvc.perform(post("/libros")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sinTitulo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Datos de entrada no válidos"));

        verify(service, never()).registrar(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crearRechazaUnLibroSinAnio() throws Exception {
        String json = "{\"titulo\":\"Dune\",\"autor\":\"Frank Herbert\"}";

        mockMvc.perform(post("/libros")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizarModificaUnLibroExistente() throws Exception {
        Libro cambios = new Libro(null, "El Quijote (edición revisada)", "Miguel de Cervantes", 1605);
        Libro actualizado = new Libro(1L, "El Quijote (edición revisada)", "Miguel de Cervantes", 1605);
        when(service.actualizar(eq(1L), any(Libro.class))).thenReturn(actualizado);

        mockMvc.perform(put("/libros/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambios)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("El Quijote (edición revisada)"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizarDevuelve404SiNoExiste() throws Exception {
        Libro cambios = new Libro(null, "No existe", "Nadie", 2000);
        when(service.actualizar(eq(99L), any(Libro.class))).thenThrow(new LibroNoEncontradoException(99L));

        mockMvc.perform(put("/libros/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambios)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarBorraUnLibroExistente() throws Exception {
        mockMvc.perform(delete("/libros/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(service).eliminar(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarDevuelve404SiNoExiste() throws Exception {
        doThrow(new LibroNoEncontradoException(99L)).when(service).eliminar(99L);

        mockMvc.perform(delete("/libros/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
