package com.uq.happypet.service;

import com.uq.happypet.model.Usuario;
import com.uq.happypet.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String registrar(Usuario usuario) {

        if (usuarioRepository.findByUsername(usuario.getUsername()).isPresent()) {
            return "USERNAME_EXISTE";
        }

        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            return "CORREO_EXISTE";
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        usuarioRepository.save(usuario);
        return "OK";
    }

    public Usuario buscarPorUsername(String username){
        return usuarioRepository.findByUsername(username).orElse(null);
    }

    public void actualizar(Long id, String nombre, String correo){

        Usuario usuario = usuarioRepository.findById(id).orElseThrow();

        usuario.setNombre(nombre);
        usuario.setCorreo(correo);

        usuarioRepository.save(usuario);
    }

    public Usuario buscarPorCorreo(String correo){
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }

    public void actualizarPassword(Long id, String password){

        Usuario usuario = usuarioRepository.findById(id).orElseThrow();

        usuario.setPassword(passwordEncoder.encode(password));

        usuarioRepository.save(usuario);
    }
}