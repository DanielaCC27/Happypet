package com.uq.happypet.service;

import com.uq.happypet.dto.RegisterRequest;
import com.uq.happypet.model.PasswordResetToken;
import com.uq.happypet.model.Usuario;
import com.uq.happypet.model.VerificationToken;
import com.uq.happypet.repository.CarritoRepository;
import com.uq.happypet.repository.PasswordResetTokenRepository;
import com.uq.happypet.repository.PedidoRepository;
import com.uq.happypet.repository.UsuarioRepository;
import com.uq.happypet.repository.VerificationTokenRepository;
import com.uq.happypet.util.PasswordPolicies;
import com.uq.happypet.util.SecureTokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PedidoRepository pedidoRepository;
    private final CarritoRepository carritoRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.email-verification-ttl-hours:24}")
    private int emailVerificationTtlHours;

    @Value("${app.password-reset-ttl-minutes:60}")
    private int passwordResetTtlMinutes;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          VerificationTokenRepository verificationTokenRepository,
                          PasswordResetTokenRepository passwordResetTokenRepository,
                          PedidoRepository pedidoRepository,
                          CarritoRepository carritoRepository,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.pedidoRepository = pedidoRepository;
        this.carritoRepository = carritoRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public RegistrationOutcome register(RegisterRequest request) {
        String correoNormalizado = request.getCorreo().trim();
        String usernameNormalizado = request.getUsername().trim();

        if (usuarioRepository.findByUsername(usernameNormalizado).isPresent()) {
            return RegistrationOutcome.USERNAME_EXISTS;
        }
        if (usuarioRepository.findByCorreoIgnoreCase(correoNormalizado).isPresent()) {
            return RegistrationOutcome.EMAIL_EXISTS;
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setCorreo(correoNormalizado);
        usuario.setUsername(usernameNormalizado);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRole("ROLE_CLIENTE");
        usuario.setEnabled(false);

        Usuario saved = usuarioRepository.save(usuario);

        String token = UUID.randomUUID().toString();
        Instant expires = Instant.now().plus(Duration.ofHours(emailVerificationTtlHours));

        saved.getVerificationTokens().clear();
        VerificationToken verificationToken = new VerificationToken(token, expires, saved);
        saved.getVerificationTokens().add(verificationToken);
        usuarioRepository.save(saved);

        boolean enviado = emailService.sendVerificationEmail(saved.getCorreo(), token, saved.getNombre());
        if (!enviado) {
            log.error("Failed to send verification email to {}. User was created; token printed to console.",
                    saved.getCorreo());
            System.out.println("Verification token for " + saved.getCorreo() + ": " + token);
        }

        return RegistrationOutcome.SUCCESS;
    }

    @Transactional
    public VerifyEmailOutcome verificarEmailPorToken(String token) {
        if (token == null || token.isBlank()) {
            return VerifyEmailOutcome.INVALID;
        }
        Optional<VerificationToken> opt = verificationTokenRepository.findByToken(token.trim());
        if (opt.isEmpty()) {
            return VerifyEmailOutcome.INVALID;
        }
        VerificationToken verificationToken = opt.get();
        Usuario usuario = verificationToken.getUsuario();

        if (usuario.isAccountActive()) {
            usuario.getVerificationTokens().remove(verificationToken);
            usuarioRepository.save(usuario);
            return VerifyEmailOutcome.ALREADY_VERIFIED;
        }

        if (Instant.now().isAfter(verificationToken.getExpiresAt())) {
            usuario.getVerificationTokens().remove(verificationToken);
            usuarioRepository.save(usuario);
            return VerifyEmailOutcome.EXPIRED;
        }

        usuario.setEnabled(true);
        usuario.getVerificationTokens().remove(verificationToken);
        usuarioRepository.save(usuario);

        return VerifyEmailOutcome.SUCCESS;
    }

    @Transactional
    public void solicitarRecuperacionPassword(String correo) {
        if (correo == null || correo.isBlank()) {
            return;
        }
        usuarioRepository.findByCorreoIgnoreCase(correo.trim()).ifPresent(usuario -> {
            passwordResetTokenRepository.deleteAllByUsuario(usuario);
            String t = SecureTokenGenerator.newToken();
            Instant expires = Instant.now().plus(Duration.ofMinutes(passwordResetTtlMinutes));
            passwordResetTokenRepository.save(new PasswordResetToken(t, expires, usuario));
            emailService.enviarRecuperacionPassword(usuario.getCorreo(), usuario.getNombre(), t);
        });
    }

    @Transactional(readOnly = true)
    public boolean tokenRestablecimientoValido(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Optional<PasswordResetToken> opt = passwordResetTokenRepository.findByToken(token.trim());
        if (opt.isEmpty()) {
            return false;
        }
        return Instant.now().isBefore(opt.get().getExpiresAt());
    }

    @Transactional
    public String restablecerPassword(String token, String nuevaPassword) {
        if (token == null || token.isBlank()) {
            return "INVALIDO";
        }
        Optional<PasswordResetToken> opt = passwordResetTokenRepository.findByToken(token.trim());
        if (opt.isEmpty()) {
            return "INVALIDO";
        }
        PasswordResetToken resetToken = opt.get();
        if (Instant.now().isAfter(resetToken.getExpiresAt())) {
            passwordResetTokenRepository.delete(resetToken);
            return "EXPIRADO";
        }
        Usuario usuario = resetToken.getUsuario();
        if (!Pattern.compile(PasswordPolicies.PATTERN).matcher(nuevaPassword).matches()) {
            return "DEBIL";
        }
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        passwordResetTokenRepository.delete(resetToken);
        return "OK";
    }

    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username).orElse(null);
    }

    public void actualizar(Long id, String nombre, String correo) {

        Usuario usuario = usuarioRepository.findById(id).orElseThrow();

        usuario.setNombre(nombre);
        usuario.setCorreo(correo);

        usuarioRepository.save(usuario);
    }

    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }

    public void actualizarPassword(Long id, String password) {

        Usuario usuario = usuarioRepository.findById(id).orElseThrow();

        usuario.setPassword(passwordEncoder.encode(password));

        usuarioRepository.save(usuario);
    }

    @Transactional
    public void cambiarPassword(String username, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado."));
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }
        if (!Pattern.compile(PasswordPolicies.PATTERN).matcher(passwordNueva).matches()) {
            throw new IllegalArgumentException(PasswordPolicies.MESSAGE);
        }
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario registrarOCargarGoogle(String email, String nombreVisible, String oauthSubject) {
        if (email == null || email.isBlank() || oauthSubject == null || oauthSubject.isBlank()) {
            throw new IllegalArgumentException("Datos de Google incompletos.");
        }
        String correo = email.trim();
        Optional<Usuario> porSub = usuarioRepository.findByOauthSubject(oauthSubject);
        if (porSub.isPresent()) {
            return porSub.get();
        }
        Optional<Usuario> porCorreo = usuarioRepository.findByCorreoIgnoreCase(correo);
        if (porCorreo.isPresent()) {
            Usuario u = porCorreo.get();
            u.setOauthProvider("google");
            u.setOauthSubject(oauthSubject);
            if (!u.isAccountActive()) {
                u.setEnabled(true);
            }
            return usuarioRepository.save(u);
        }
        String nombre = (nombreVisible != null && !nombreVisible.isBlank()) ? nombreVisible.trim() : correo;
        String base = correo.replace('@', '_').replaceAll("[^a-zA-Z0-9._-]", "_");
        if (base.length() < 3) {
            base = "user_" + oauthSubject.substring(0, Math.min(8, oauthSubject.length()));
        }
        String username = base;
        int suf = 0;
        while (usuarioRepository.findByUsername(username).isPresent()) {
            suf++;
            username = base + "_" + suf;
        }
        Usuario nuevo = new Usuario();
        nuevo.setNombre(nombre);
        nuevo.setCorreo(correo);
        nuevo.setUsername(username);
        nuevo.setPassword(passwordEncoder.encode(UUID.randomUUID() + "Aa!1"));
        nuevo.setRole("ROLE_CLIENTE");
        nuevo.setEnabled(true);
        nuevo.setOauthProvider("google");
        nuevo.setOauthSubject(oauthSubject);
        return usuarioRepository.save(nuevo);
    }

    public void eliminarCuenta(Long id, String usernameActual) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        if (!usuario.getUsername().equals(usernameActual)) {
            throw new IllegalArgumentException("No autorizado");
        }

        pedidoRepository.deleteAll(pedidoRepository.findByUsuario(usuario));
        carritoRepository.deleteAll(carritoRepository.findByUsuario(usuario));
        passwordResetTokenRepository.deleteAllByUsuario(usuario);
        verificationTokenRepository.deleteAllByUsuario(usuario);
        usuarioRepository.delete(usuario);
    }
}