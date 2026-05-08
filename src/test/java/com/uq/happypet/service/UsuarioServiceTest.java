package com.uq.happypet.service;

import com.uq.happypet.dto.RegisterRequest;
import com.uq.happypet.model.Usuario;
import com.uq.happypet.repository.CarritoRepository;
import com.uq.happypet.repository.PasswordResetTokenRepository;
import com.uq.happypet.repository.PedidoRepository;
import com.uq.happypet.repository.UsuarioRepository;
import com.uq.happypet.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private CarritoRepository carritoRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private UsuarioService usuarioService;

    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest("Maria", "maria@test.com", "maria", "Valida123!");
    }

    @Test
    void register_exitoso_creaUsuarioDeshabilitadoYRetornaSuccess() {
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCorreoIgnoreCase("maria@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Valida123!")).thenReturn("encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailService.sendVerificationEmail(any(), any(), any())).thenReturn(true);

        RegistrationOutcome outcome = usuarioService.register(request);

        assertEquals(RegistrationOutcome.SUCCESS, outcome);
        verify(usuarioRepository, times(2)).save(any(Usuario.class));
        verify(emailService).sendVerificationEmail(any(), any(), any());
    }

    @Test
    void register_usernameDuplicado_retornaUsernameExists() {
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(new Usuario()));

        RegistrationOutcome outcome = usuarioService.register(request);

        assertEquals(RegistrationOutcome.USERNAME_EXISTS, outcome);
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
    }

    @Test
    void register_correoDuplicado_retornaEmailExists() {
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCorreoIgnoreCase("maria@test.com")).thenReturn(Optional.of(new Usuario()));

        RegistrationOutcome outcome = usuarioService.register(request);

        assertEquals(RegistrationOutcome.EMAIL_EXISTS, outcome);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void actualizar_perfilExistente_persisteCambios() {
        Usuario usuario = new Usuario("Anterior", "old@test.com", "maria", "x", "ROLE_CLIENTE");
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));

        usuarioService.actualizar(7L, "Nuevo Nombre", "nuevo@test.com");

        assertEquals("Nuevo Nombre", usuario.getNombre());
        assertEquals("nuevo@test.com", usuario.getCorreo());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void actualizar_perfilInexistente_lanzaExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> usuarioService.actualizar(99L, "N", "c@test.com"));
    }

    @Test
    void cambiarPassword_exitoso_persisteNuevaContrasena() {
        Usuario usuario = new Usuario("N", "n@test.com", "maria", "hashActual", "ROLE_CLIENTE");
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(eq("Actual123!"), eq("hashActual"))).thenReturn(true);
        when(passwordEncoder.encode("Nueva123!")).thenReturn("hashNuevo");

        usuarioService.cambiarPassword("maria", "Actual123!", "Nueva123!");

        assertEquals("hashNuevo", usuario.getPassword());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarPassword_actualIncorrecta_lanza() {
        Usuario usuario = new Usuario("N", "n@test.com", "maria", "hashActual", "ROLE_CLIENTE");
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(any(), eq("hashActual"))).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.cambiarPassword("maria", "mala", "Nueva123!"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarPassword_nuevaDebil_lanza() {
        Usuario usuario = new Usuario("N", "n@test.com", "maria", "hashActual", "ROLE_CLIENTE");
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(eq("Actual123!"), eq("hashActual"))).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.cambiarPassword("maria", "Actual123!", "debil"));
        verify(usuarioRepository, never()).save(any());
    }
}
