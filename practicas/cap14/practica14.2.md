# Práctica 14.2 — Refresh token

## Enunciado

Los tokens JWT de corta duración (1 hora) mejoran la seguridad, pero obligan al usuario a
hacer login frecuentemente. Implementa un mecanismo de refresh:

1. En `POST /auth/login`, devuelve dos tokens: `accessToken` (expira en 15 minutos) y
   `refreshToken` (expira en 7 días), cada uno con un propósito (`typ` claim: `"access"` y
   `"refresh"`).
2. Crea el endpoint `POST /auth/refresh` que recibe el `refreshToken` en el cuerpo, lo
   valida (incluyendo verificar que `typ == "refresh"`), y devuelve un nuevo
   `accessToken`.
3. `JwtFiltro` debe rechazar peticiones que usen un `refreshToken` como `accessToken`
   (verificar que `typ == "access"`).
4. Permite `/auth/refresh` en la lista de rutas públicas.

## Solución

### 1. `JwtServicio` generando los dos tipos de token

```java
@Service
public class JwtServicio {

    private final SecretKey clave;
    private final long accessExpiracionMs;
    private final long refreshExpiracionMs;

    public JwtServicio(@Value("${jwt.secreto}") String secreto,
                        @Value("${jwt.access-expiracion-ms}") long accessExpiracionMs,
                        @Value("${jwt.refresh-expiracion-ms}") long refreshExpiracionMs) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.accessExpiracionMs = accessExpiracionMs;
        this.refreshExpiracionMs = refreshExpiracionMs;
    }

    public String generarAccessToken(String username, String rol) {
        return generarToken(username, rol, "access", accessExpiracionMs);
    }

    public String generarRefreshToken(String username, String rol) {
        return generarToken(username, rol, "refresh", refreshExpiracionMs);
    }

    private String generarToken(String username, String rol, String tipo, long expiracionMs) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .claim("typ", tipo)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(clave)
                .compact();
    }

    public String extraerUsername(String token) {
        return parsearClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        return parsearClaims(token).get("rol", String.class);
    }

    public String extraerTipo(String token) {
        return parsearClaims(token).get("typ", String.class);
    }

    public boolean esValido(String token) {
        try {
            parsearClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

`generarToken(...)` pasa a ser un método privado parametrizado por tipo y duración —
`generarAccessToken`/`generarRefreshToken` son la única API pública que usa el resto de la
aplicación, así que ningún llamador puede construir un token sin pasar por uno de los dos
propósitos previstos.

`application-dev.properties`, sustituyendo la propiedad única de expiración:

```properties
# jwt.expiracion-ms=3600000   -- sustituida por las dos siguientes
jwt.access-expiracion-ms=900000
jwt.refresh-expiracion-ms=604800000
```

(`900000` ms = 15 minutos; `604800000` ms = 7 días.)

### 2. Login devuelve los dos tokens

`RespuestaLogin`, nuevo, en el paquete `seguridad`:

```java
package com.javaesencial.biblioteca.seguridad;

public record RespuestaLogin(String accessToken, String refreshToken, String tipo) {

    public RespuestaLogin(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, "Bearer");
    }
}
```

`RespuestaToken` (la existente, con un solo `token`) se mantiene sin cambios: la usa el
endpoint de refresh para devolver solo el `accessToken` nuevo.

`AuthController.login`:

```java
@PostMapping("/login")
public RespuestaLogin login(@RequestBody PeticionLogin peticion) {
    Credencial credencial = credenciales.findByUsername(peticion.username())
            .filter(c -> passwordEncoder.matches(peticion.password(), c.getPassword()))
            .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));

    String accessToken = jwtServicio.generarAccessToken(credencial.getUsername(), credencial.getRol());
    String refreshToken = jwtServicio.generarRefreshToken(credencial.getUsername(), credencial.getRol());
    return new RespuestaLogin(accessToken, refreshToken);
}
```

### 3. Endpoint `POST /auth/refresh`

`PeticionRefresh`, nuevo:

```java
package com.javaesencial.biblioteca.seguridad;

public record PeticionRefresh(String refreshToken) {
}
```

`AuthController.refresh`:

```java
@PostMapping("/refresh")
public RespuestaToken refresh(@RequestBody PeticionRefresh peticion) {
    String refreshToken = peticion.refreshToken();

    if (!jwtServicio.esValido(refreshToken) || !"refresh".equals(jwtServicio.extraerTipo(refreshToken))) {
        throw new BadCredentialsException("Refresh token inválido, expirado o de tipo incorrecto");
    }

    String username = jwtServicio.extraerUsername(refreshToken);
    String rol = jwtServicio.extraerRol(refreshToken);
    String nuevoAccessToken = jwtServicio.generarAccessToken(username, rol);
    return new RespuestaToken(nuevoAccessToken);
}
```

Reutiliza `BadCredentialsException`, la misma excepción que ya lanza `login` cuando las
credenciales no son válidas — `ManejadorGlobalErrores.manejarCredencialesInvalidas` (ya
existente en el cuerpo del capítulo) la traduce a un 401 sin necesidad de ningún manejador
nuevo: un refresh token inválido es, conceptualmente, el mismo tipo de fallo que un
usuario/contraseña incorrectos — "no puedo confirmar que tengas derecho a lo que pides".

### 4. `JwtFiltro` rechazando un refresh token como access token

```java
if (cabecera != null && cabecera.startsWith("Bearer ")) {
    String token = cabecera.substring(7);

    if (jwtServicio.esValido(token) && "access".equals(jwtServicio.extraerTipo(token))) {
        String username = jwtServicio.extraerUsername(token);
        String rol = jwtServicio.extraerRol(token);

        UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
        SecurityContextHolder.getContext().setAuthentication(autenticacion);
    }
}
```

Sin esta comprobación, un `refreshToken` —que también tiene una firma válida, porque lo
firma la misma clave— autenticaría igual que un `accessToken` en cualquier endpoint
protegido, contradiciendo su propósito: un token pensado solo para renovar el acceso, con
una vida mucho más larga (7 días), quedaría utilizable directamente como credencial de
acceso a toda la API. Comprobar `typ == "access"` es lo que separa realmente los dos usos.

### 5. Ruta pública

```java
.requestMatchers("/auth/**").permitAll()
```

Este `requestMatchers` ya cubre `/auth/refresh` sin ningún cambio: usa el comodín
`/auth/**`, que ya incluye `/auth/login` y `/auth/logout` (Práctica 14.1) en el cuerpo del
capítulo — no hace falta añadir una línea específica para `/auth/refresh`.

### Comprobación

```terminal
POST /auth/login
{ "username": "admin", "password": "admin123" }
```
```json
{ "accessToken": "eyJhbGci...(15 min)", "refreshToken": "eyJhbGci...(7 días)", "tipo": "Bearer" }
```

```terminal
GET /prestamos
Authorization: Bearer eyJhbGci...(refreshToken)
```
```terminal
HTTP/1.1 401
```
(el `refreshToken` tiene firma válida, pero `typ` es `"refresh"`, no `"access"` — `JwtFiltro`
no autentica la petición.)

```terminal
POST /auth/refresh
{ "refreshToken": "eyJhbGci...(7 días)" }
```
```json
{ "token": "eyJhbGci...(nuevo accessToken, 15 min)", "tipo": "Bearer" }
```

```terminal
GET /prestamos
Authorization: Bearer eyJhbGci...(nuevo accessToken)
```
```terminal
HTTP/1.1 200
```
