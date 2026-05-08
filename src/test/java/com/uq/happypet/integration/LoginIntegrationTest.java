package com.uq.happypet.integration;

import com.uq.happypet.model.Usuario;
import com.uq.happypet.repository.UsuarioRepository;
import com.uq.happypet.testsupport.JavaMailSenderTestStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(JavaMailSenderTestStub.class)
@Transactional
class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepository.findByUsername("cliente").ifPresent(usuarioRepository::delete);
        Usuario user = new Usuario("Cliente Happy", "cliente@test.com", "cliente",
                passwordEncoder.encode("Valida123!"), "ROLE_CLIENTE");
        user.setEnabled(true);
        usuarioRepository.save(user);
    }

    @Test
    void login_exitoso_redirigeHome() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "cliente")
                        .param("password", "Valida123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    void login_invalido_redirigeConError() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "cliente")
                        .param("password", "incorrecta"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }
}
