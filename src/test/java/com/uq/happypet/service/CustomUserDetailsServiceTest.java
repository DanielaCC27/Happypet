package com.uq.happypet.service;

import com.uq.happypet.model.Usuario;
import com.uq.happypet.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void deberiaCargarUsuarioCorrectamente() {

        Usuario usuario = new Usuario(
                "Admin",
                "admin@correo.com",
                "admin",
                "1234",
                "ROLE_ADMIN"
        );

        when(usuarioRepository.findByUsername("admin"))
                .thenReturn(Optional.of(usuario));

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername("admin");

        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
    }

    @Test
    void deberiaLanzarErrorSiUsuarioNoExiste() {

        when(usuarioRepository.findByUsername("noExiste"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("noExiste")
        );
    }
}