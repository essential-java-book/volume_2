# Práctica 14.1 — Endpoint de logout con lista negra en memoria

## Enunciado

Implementa un endpoint `POST /auth/logout` que invalide el token actual. Como JWT es sin
estado y no hay sesión en el servidor, usa una lista negra en memoria (`Set<String>`) que
almacene los JTI (JWT ID) de los tokens revocados.

Pasos:
1. Añade la claim `jti` al `generarToken` en `JwtServicio` usando
   `UUID.randomUUID().toString()`.
2. Crea un `ListaNegraTokensService` (un `@Component` con un `Set<String>` thread-safe
   usando `ConcurrentHashMap.newKeySet()`).
3. En `POST /auth/logout`, extrae el `jti` del token del header `Authorization` y añádelo
   a la lista negra.
4. En `JwtFiltro`, tras validar el token, comprueba que su `jti` no esté en la lista
   negra.
5. Verifica que después de logout el mismo token devuelve 401.

## Solución

### 1. `JwtServicio` con la claim `jti`

```java
public String generarToken(String username, String rol) {
    Date ahora = new Date();
    Date expiracion = new Date(ahora.getTime() + expiracionMs);

    return Jwts.builder()
            .subject(username)
            .claim("rol", rol)
            .id(UUID.randomUUID().toString())
            .issuedAt(ahora)
            .expiration(expiracion)
            .signWith(clave)
            .compact();
}

public String extraerJti(String token) {
    return parsearClaims(token).getId();
}
```

Se usa `.id(...)` en el builder (y `Claims.getId()` al leerlo) en vez de
`.claim("jti", ...)`/`.get("jti", String.class)`: `jti` es una *claim registrada* del
estándar JWT (RFC 7519), igual que `sub` (`.subject(...)`) o `exp` (`.expiration(...)`), y
la API fluida de `jjwt` expone un método dedicado para cada una — `.claim(nombre, valor)`
queda reservado para claims *propias*, como ya hace este mismo método con `"rol"`.

### 2. `ListaNegraTokensService`

```java
package com.javaesencial.biblioteca.seguridad;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lista negra de JTI revocados, en memoria. Como @Component es un
 * singleton por defecto, AuthController y JwtFiltro comparten la
 * misma instancia (y el mismo Set) sin necesidad de pasarla
 * explícitamente entre ellos.
 *
 * Limitación deliberada de esta práctica: en memoria, se pierde al
 * reiniciar la aplicación, y no se comparte entre varias instancias
 * si la API se llega a desplegar replicada -- una lista negra real en
 * producción necesitaría un almacén compartido (Redis con TTL, por
 * ejemplo), fuera del alcance de este ejercicio.
 */
@Component
public class ListaNegraTokensService {

    private final Set<String> jtisRevocados = ConcurrentHashMap.newKeySet();

    public void revocar(String jti) {
        jtisRevocados.add(jti);
    }

    public boolean estaRevocado(String jti) {
        return jtisRevocados.contains(jti);
    }
}
```

### 3. `AuthController` con `POST /auth/logout`

```java
private final ListaNegraTokensService listaNegra;

public AuthController(CredencialJpaRepository credenciales,
                       PasswordEncoder passwordEncoder,
                       JwtServicio jwtServicio,
                       ListaNegraTokensService listaNegra) {
    this.credenciales = credenciales;
    this.passwordEncoder = passwordEncoder;
    this.jwtServicio = jwtServicio;
    this.listaNegra = listaNegra;
}

@PostMapping("/logout")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void logout(@RequestHeader("Authorization") String cabecera) {
    String token = cabecera.substring(7); // quita el prefijo "Bearer "
    String jti = jwtServicio.extraerJti(token);
    listaNegra.revocar(jti);
}
```

### 4. `JwtFiltro` comprobando la lista negra

```java
private final JwtServicio jwtServicio;
private final ListaNegraTokensService listaNegra;

public JwtFiltro(JwtServicio jwtServicio, ListaNegraTokensService listaNegra) {
    this.jwtServicio = jwtServicio;
    this.listaNegra = listaNegra;
}

@Override
protected void doFilterInternal(HttpServletRequest request,
                                 HttpServletResponse response,
                                 FilterChain chain) throws ServletException, IOException {

    String cabecera = request.getHeader("Authorization");

    if (cabecera != null && cabecera.startsWith("Bearer ")) {
        String token = cabecera.substring(7);

        if (jwtServicio.esValido(token) && !listaNegra.estaRevocado(jwtServicio.extraerJti(token))) {
            String username = jwtServicio.extraerUsername(token);
            String rol = jwtServicio.extraerRol(token);

            UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                    username, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
            SecurityContextHolder.getContext().setAuthentication(autenticacion);
        }
    }

    chain.doFilter(request, response);
}
```

El orden del `&&` importa: `jwtServicio.esValido(token)` se comprueba primero porque
`extraerJti(token)` vuelve a analizar (`parsearClaims`) el token — llamarlo sobre un token
ya inválido o mal formado lanzaría la misma excepción que `esValido` ya se encarga de
capturar internamente; solo tiene sentido preguntar "¿está en la lista negra?" de un token
que ya se sabe estructuralmente válido y con firma correcta.

### 5. Comprobación

```terminal
POST /auth/login
{ "username": "admin", "password": "admin123" }
```
```json
{ "token": "eyJhbGci...", "tipo": "Bearer" }
```

```terminal
GET /prestamos
Authorization: Bearer eyJhbGci...
```
```terminal
HTTP/1.1 200
```

```terminal
POST /auth/logout
Authorization: Bearer eyJhbGci...
```
```terminal
HTTP/1.1 204
```

```terminal
GET /prestamos
Authorization: Bearer eyJhbGci...
```
```terminal
HTTP/1.1 401
```

El mismo token, sintácticamente idéntico y con firma todavía válida (`esValido(...)`
seguiría devolviendo `true`: la revocación no invalida la firma, solo lo marca como no
utilizable), deja de autenticar tras el logout — `JwtFiltro` no rellena el
`SecurityContextHolder`, así que la petición llega sin autenticación al resto de la
cadena, con el mismo resultado 401 que documenta el capítulo para cualquier token
inválido.
