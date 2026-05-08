package com.uq.happypet.api;

import com.uq.happypet.dto.RegisterRequest;
import com.uq.happypet.service.RegistrationOutcome;
import com.uq.happypet.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class AuthApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void register_exitoso_retornaCreated() throws Exception {
        RegisterRequest req = new RegisterRequest("Ana", "ana@test.com", "ana", "Valida123!");
        when(usuarioService.register(any(RegisterRequest.class))).thenReturn(RegistrationOutcome.SUCCESS);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists());

        verify(usuarioService).register(any(RegisterRequest.class));
    }

    @Test
    void register_usernameDuplicado_retornaConflict() throws Exception {
        RegisterRequest req = new RegisterRequest("Ana", "ana@test.com", "ana", "Valida123!");
        when(usuarioService.register(any(RegisterRequest.class))).thenReturn(RegistrationOutcome.USERNAME_EXISTS);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("USERNAME_EXISTS"));
    }

    @Test
    void register_camposInvalidos_retornaBadRequest() throws Exception {
        String bodyInvalido = """
                {
                  "nombre": "",
                  "correo": "correo-invalido",
                  "username": "a",
                  "password": "123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
