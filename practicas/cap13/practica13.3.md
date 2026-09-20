# Práctica 13.3 — Ampliar la cobertura de tests de seguridad

## Enunciado

> **Nota:** el enunciado de esta práctica se corrigió durante la revisión de la
> colección — en el manuscrito original pedía tests que, en realidad, ya forman parte del
> cuerpo corregido del capítulo (sección 13.4-bis, `LibroControllerTest` actualizado). El
> enunciado vigente es:

`LibroControllerTest` (sección 13.4-bis) ya cubre el caso de 401 implícito al eliminar
`@WithMockUser` de los tests de escritura, y el caso de acceso público en los tests de
lectura. Como ampliación, añade un test explícito que verifique que `POST /libros`
**sin** `@WithMockUser` devuelve `401 Unauthorized`, y otro que verifique que un usuario
con `@WithMockUser(roles = "USER")` recibe `403 Forbidden` en el mismo endpoint (en vez de
`401`, porque sí está autenticado, solo que sin el rol necesario).

## Solución

Los dos tests nuevos, añadidos a `LibroControllerTest` (sección 13.4-bis):

```java
@Test
void crearSinAutenticarDevuelve401() throws Exception {
    Libro entrada = new Libro(null, "Dune", "Frank Herbert", 1965);

    mockMvc.perform(post("/libros")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(entrada)))
            .andExpect(status().isUnauthorized());

    verify(service, never()).registrar(any());
}

@Test
@WithMockUser(roles = "USER")
void crearConRolUserDevuelve403() throws Exception {
    Libro entrada = new Libro(null, "Dune", "Frank Herbert", 1965);

    mockMvc.perform(post("/libros")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(entrada)))
            .andExpect(status().isForbidden());

    verify(service, never()).registrar(any());
}
```

Ambos añaden `verify(service, never()).registrar(any())`: en los dos casos la petición
debe rechazarse **antes** de llegar al controlador (el filtro de seguridad la intercepta),
así que `BibliotecaService.registrar` no debería llegar a invocarse nunca — es la forma de
comprobar, no solo el código de estado HTTP, sino que la autorización realmente bloquea la
operación en la capa de seguridad, no en la lógica de negocio.

### Por qué 401 en un test y 403 en el otro

El primer test no lleva ninguna anotación de autenticación (`@WithMockUser` ausente): la
petición llega sin ninguna identidad, así que Spring Security responde `401 Unauthorized`
—"no sé quién eres"— antes de que la regla `hasRole("ADMIN")` llegue siquiera a
evaluarse, porque esa regla presupone que ya hay alguien autenticado a quien
comprobarle el rol.

El segundo test sí lleva `@WithMockUser(roles = "USER")`: la petición llega con una
identidad válida —Spring Security sabe perfectamente quién es—, pero esa identidad no
tiene el rol `ADMIN` que exige `hasRole("ADMIN")` en `POST /libros`, así que la respuesta
es `403 Forbidden` —"sé quién eres, y no tienes permiso"—. Es la misma distinción que ya
repasa la Práctica 13.1 contra el proyecto real (`admin` frente a `bibliotecario`), aquí
expresada como dos tests de `@WebMvcTest` en vez de dos peticiones manuales con
`pruebas.http`.

### Por qué `.with(csrf())` incluso en el test que espera 401

`SeguridadConfig` deshabilita CSRF por completo (`.csrf(csrf -> csrf.disable())`, sección
13.4 — coherente con una API sin estado, autenticada por cabecera en cada petición, no por
cookies de sesión), así que `.with(csrf())` no cambia aquí el resultado de ningún test:
ni ayuda a autenticar, ni evita ningún rechazo por CSRF, porque ese filtro ni siquiera está
activo. Se mantiene en los dos tests nuevos solo por coherencia con el resto de
`LibroControllerTest` (sección 13.4-bis), donde todas las peticiones de escritura ya lo
incluyen — y como hábito defensivo: si algún día se reactivara CSRF (por ejemplo, al pasar
esta API a un cliente con sesión y cookies en vez de credenciales por cabecera), estos
tests seguirían pasando sin tener que revisarlos.
