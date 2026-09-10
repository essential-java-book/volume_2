package com.javaesencial.biblioteca.seguridad;

import com.javaesencial.biblioteca.dominio.Credencial;
import com.javaesencial.biblioteca.repositorio.CredencialJpaRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Traduce una {@code Credencial} de la base de datos al
 * {@code UserDetails} que Spring Security necesita para autenticar.
 * {@code credencial.rol} guarda "ADMIN"/"USER" sin el prefijo
 * "ROLE_"; {@code User.builder().roles(...)} lo añade solo.
 */
@Service
public class BibliotecaUserDetailsService implements UserDetailsService {

    private final CredencialJpaRepository credenciales;

    public BibliotecaUserDetailsService(CredencialJpaRepository credenciales) {
        this.credenciales = credenciales;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Credencial credencial = credenciales.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return User.builder()
                .username(credencial.getUsername())
                .password(credencial.getPassword())
                .roles(credencial.getRol())
                .build();
    }
}
