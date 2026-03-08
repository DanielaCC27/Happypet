package com.uq.happypet.repository;

import com.uq.happypet.model.Usuario;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioRepositoryTest {

    @Test
    void deberiaEncontrarUsuarioPorUsername() {

        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);

        Usuario usuario = new Usuario(
                "Admin",
                "admin@correo.com",
                "adminTest",
                "1234",
                "ROLE_ADMIN"
        );

        when(usuarioRepository.findByUsername("adminTest"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado =
                usuarioRepository.findByUsername("adminTest");

        assertTrue(resultado.isPresent());
        assertEquals("adminTest", resultado.get().getUsername());
    }
}