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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Buscar usuario en la base de datos
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Crear el objeto User que Spring Security necesita
        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                Collections.singletonList(
                        new SimpleGrantedAuthority(usuario.getRole())
                )
        );
    }
}

