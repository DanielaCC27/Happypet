package com.uq.happypet.service;

import com.uq.happypet.model.Usuario;
import com.uq.happypet.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Carga la información del usuario desde la base de datos
     * para el proceso de autenticación de Spring Security.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Busca el usuario por username
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado"));

        /*
         * Construcción del objeto UserDetails utilizado
         * por Spring Security durante la autenticación.
         */
        return org.springframework.security.core.userdetails.User
                .withUsername(usuario.getUsername())
                .password(usuario.getPassword())

                // Deshabilita el acceso si la cuenta está inactiva
                .disabled(!usuario.isAccountActive())

                // Asignación de roles/permisos del usuario
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority(usuario.getRole())
                ))

                .build();
    }
}