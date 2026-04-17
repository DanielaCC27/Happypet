package com.uq.happypet.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/",
                                "/home",
                                "/login",
                                "/usuarios/registro",
                                "/usuarios/registrar",
                                "/usuarios/recuperar",
                                "/usuarios/solicitar-recuperacion",
                                "/usuarios/verificar-email",
                                "/usuarios/restablecer-password",
                                "/reset-password",
                                "/reset-password/**",
                                "/api/auth/register",
                                "/api/auth/verify",
                                "/api/auth/reset-password",
                                "/api/auth/forgot-password",
                                "/api/test/mail/**",
                                "/css/**",
                                "/js/**",
                                "/data/**",
                                "/api/ubicaciones/colombia"
                        ).permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .requestMatchers("/productos/nuevo").hasRole("ADMIN")
                        .requestMatchers("/productos/guardar").hasRole("ADMIN")
                        .requestMatchers("/productos/editar/**").hasRole("ADMIN")
                        .requestMatchers("/productos/eliminar/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/productos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/productos/**").permitAll()

                        .anyRequest().authenticated()
                )

                .formLogin(login -> login
                        .loginPage("/login")
                        .successHandler(loginSuccessHandler())
                        .failureHandler(loginFailureHandler())
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    /**
     * Tras login correcto siempre se lleva a /home (catálogo), sin depender de la URL guardada.
     */
    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/home");
        handler.setAlwaysUseDefaultTargetUrl(true);
        return handler;
    }

    @Bean
    public AuthenticationFailureHandler loginFailureHandler() {
        return new AuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request,
                                                HttpServletResponse response,
                                                AuthenticationException exception) {
                try {
                    if (exception instanceof DisabledException) {
                        response.sendRedirect(request.getContextPath() + "/login?unverified");
                        return;
                    }
                    response.sendRedirect(request.getContextPath() + "/login?error");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
