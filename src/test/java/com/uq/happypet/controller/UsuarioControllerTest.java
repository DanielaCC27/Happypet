package com.uq.happypet.controller;

import com.uq.happypet.dto.ChangePasswordRequest;
import com.uq.happypet.dto.RegisterRequest;
import com.uq.happypet.model.Usuario;
import com.uq.happypet.service.RegistrationOutcome;
import com.uq.happypet.service.UsuarioService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;
    @Mock
    private Validator validator;

    @InjectMocks
    private UsuarioController usuarioController;

    @Test
    void actualizarUsuario_redirigePerfilYActualizaServicio() {
        String vista = usuarioController.actualizarUsuario(9L, "Nuevo", "nuevo@test.com");

        assertEquals("redirect:/usuarios/perfil", vista);
        verify(usuarioService).actualizar(9L, "Nuevo", "nuevo@test.com");
    }

    @Test
    void registrarUsuario_validacionFalla_mantieneFormularioConError() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<RegisterRequest> violation =
                (ConstraintViolation<RegisterRequest>) mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Correo electronico invalido");
        when(validator.validate(any(RegisterRequest.class))).thenReturn(Set.of(violation));
        ExtendedModelMap model = new ExtendedModelMap();

        String vista = usuarioController.registrarUsuario("x", "bad-email", "usr", "Valida123!", model);

        assertEquals("usuarios/register", vista);
        assertEquals("Correo electronico invalido", model.get("error"));
        verify(usuarioService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void registrarUsuario_exitoso_muestraMensajeYDelegaRegistro() {
        when(validator.validate(any(RegisterRequest.class))).thenReturn(Collections.emptySet());
        when(usuarioService.register(any(RegisterRequest.class))).thenReturn(RegistrationOutcome.SUCCESS);
        ExtendedModelMap model = new ExtendedModelMap();

        String vista = usuarioController.registrarUsuario(
                "Ana", "ana@test.com", "ana", "Valida123!", model);

        assertEquals("usuarios/register", vista);
        assertEquals(
                "Cuenta creada. Revisa tu correo y abre el enlace para verificar tu cuenta antes de iniciar sesi\u00f3n.",
                model.get("success"));
        verify(usuarioService).register(any(RegisterRequest.class));
    }

    @Test
    void registrarUsuario_correoDuplicado_muestraError() {
        when(validator.validate(any(RegisterRequest.class))).thenReturn(Collections.emptySet());
        when(usuarioService.register(any(RegisterRequest.class))).thenReturn(RegistrationOutcome.EMAIL_EXISTS);
        ExtendedModelMap model = new ExtendedModelMap();

        String vista = usuarioController.registrarUsuario(
                "Ana", "dup@test.com", "ana", "Valida123!", model);

        assertEquals("usuarios/register", vista);
        assertEquals("Este correo ya est\u00e1 registrado.", model.get("error"));
    }

    @Test
    void verPerfil_cargaUsuarioPorPrincipal() {
        Usuario usuario = new Usuario("Juan", "juan@test.com", "juan", "x", "ROLE_CLIENTE");
        when(usuarioService.buscarPorUsername("juan")).thenReturn(usuario);
        ExtendedModelMap model = new ExtendedModelMap();
        Principal principal = () -> "juan";

        String vista = usuarioController.verPerfil(model, principal);

        assertEquals("usuarios/perfil", vista);
        assertSame(usuario, model.get("usuario"));
    }

    @Test
    void cambiarPassword_datosValidos_invocaServicio() {
        when(validator.validate(any(ChangePasswordRequest.class))).thenReturn(Collections.emptySet());
        RedirectAttributesModelMap flash = new RedirectAttributesModelMap();
        Principal principal = () -> "maria";

        String vista = usuarioController.cambiarPassword("Actual123!", "Nueva123!", principal, flash);

        assertEquals("redirect:/usuarios/perfil", vista);
        verify(usuarioService).cambiarPassword("maria", "Actual123!", "Nueva123!");
    }

    @Test
    void cambiarPassword_conCamposInvalidos_seteaFlashError() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<ChangePasswordRequest> violation =
                (ConstraintViolation<ChangePasswordRequest>) mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Formato invalido");
        when(validator.validate(any(ChangePasswordRequest.class))).thenReturn(Set.of(violation));
        RedirectAttributesModelMap flash = new RedirectAttributesModelMap();
        Principal principal = () -> "maria";

        String vista = usuarioController.cambiarPassword("x", "123", principal, flash);

        assertEquals("redirect:/usuarios/perfil", vista);
        assertEquals("Formato invalido", flash.getFlashAttributes().get("passwordError"));
    }
}
