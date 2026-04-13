package com.uq.happypet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final LoginAuthenticationFailureHandler loginAuthenticationFailureHandler;

    public SecurityConfig(LoginAuthenticationFailureHandler loginAuthenticationFailureHandler) {
        this.loginAuthenticationFailureHandler = loginAuthenticationFailureHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/",
                                "/login",
                                "/auth/**",
                                "/usuarios/registro",
                                "/usuarios/registrar",
                                "/usuarios/recuperar",
                                "/css/**"
                        ).permitAll()

                        // catálogo visible para todos los usuarios autenticados
                        .requestMatchers("/productos").authenticated()

                        // SOLO ADMIN
                        .requestMatchers("/productos/nuevo").hasRole("ADMIN")
                        .requestMatchers("/productos/guardar").hasRole("ADMIN")
                        .requestMatchers("/productos/editar/**").hasRole("ADMIN")
                        .requestMatchers("/productos/eliminar/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureHandler(loginAuthenticationFailureHandler)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}