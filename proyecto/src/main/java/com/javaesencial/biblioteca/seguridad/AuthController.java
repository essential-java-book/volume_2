package com.javaesencial.biblioteca.seguridad;

import com.javaesencial.biblioteca.dominio.Credencial;
import com.javaesencial.biblioteca.repositorio.CredencialJpaRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final CredencialJpaRepository credenciales;
    private final PasswordEncoder passwordEncoder;
    private final JwtServicio jwtServicio;

    public AuthController(CredencialJpaRepository credenciales,
                           PasswordEncoder passwordEncoder,
                           JwtServicio jwtServicio) {
        this.credenciales = credenciales;
        this.passwordEncoder = passwordEncoder;
        this.jwtServicio = jwtServicio;
    }

    @PostMapping("/login")
    public RespuestaToken login(@RequestBody PeticionLogin peticion) {
        Credencial credencial = credenciales.findByUsername(peticion.username())
                .filter(c -> passwordEncoder.matches(peticion.password(), c.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));

        String token = jwtServicio.generarToken(credencial.getUsername(), credencial.getRol());
        return new RespuestaToken(token);
    }
}
