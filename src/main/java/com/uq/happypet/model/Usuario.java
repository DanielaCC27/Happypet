package com.uq.happypet.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String correo;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    /**
     * Cuenta activa tras verificar correo. Puede ser null en BD heredada; ver {@link #isAccountActive()}.
     */
    @Column(name = "enabled")
    private Boolean enabled;

    /**
     * Columna legada (solo lectura): usuarios creados antes de {@code enabled} siguen activos si estaban verificados.
     */
    @Column(name = "email_verificado", insertable = false, updatable = false)
    private Boolean emailVerificado;

    @Column(name = "oauth_provider", length = 32)
    private String oauthProvider;

    @Column(name = "oauth_subject", unique = true, length = 128)
    private String oauthSubject;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VerificationToken> verificationTokens = new ArrayList<>();

    public Usuario() {}

    public Usuario(String nombre, String correo, String username, String password, String role) {
        this.nombre = nombre;
        this.correo = correo;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /**
     * Indica si el usuario puede iniciar sesi\u00f3n (verificado / habilitado).
     */
    public boolean isAccountActive() {
        return Boolean.TRUE.equals(enabled) || Boolean.TRUE.equals(emailVerificado);
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<VerificationToken> getVerificationTokens() {
        return verificationTokens;
    }

    public void setVerificationTokens(List<VerificationToken> verificationTokens) {
        this.verificationTokens = verificationTokens;
    }

    public String getOauthProvider() {
        return oauthProvider;
    }

    public void setOauthProvider(String oauthProvider) {
        this.oauthProvider = oauthProvider;
    }

    public String getOauthSubject() {
        return oauthSubject;
    }

    public void setOauthSubject(String oauthSubject) {
        this.oauthSubject = oauthSubject;
    }
}