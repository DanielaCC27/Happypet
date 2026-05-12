package com.uq.happypet.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bean Validation rules on {@link RegisterRequest} (shared by API and web form).
 * Full HTTP register flows for the REST API live in {@link com.uq.happypet.api.AuthApiControllerTest}.
 */
class RegisterRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void camposValidos_sinViolaciones() {
        RegisterRequest r = new RegisterRequest("Maria", "maria@test.com", "maria", "Valida123!");
        assertTrue(validator.validate(r).isEmpty());
    }

    @Test
    void passwordConSimboloFueraDeListaAcotada_sinViolaciones() {
        RegisterRequest r = new RegisterRequest("Maria", "maria@test.com", "maria", "Valida123*");
        assertTrue(validator.validate(r).isEmpty());
    }

    @Test
    void correoInvalido_violacionEnCorreo() {
        RegisterRequest r = new RegisterRequest("Maria", "no-es-correo", "maria", "Valida123!");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(r);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "correo".equals(v.getPropertyPath().toString())));
    }

    @Test
    void nombreConDigitos_violacionEnNombre() {
        RegisterRequest r = new RegisterRequest("Maria99", "maria@test.com", "maria", "Valida123!");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(r);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "nombre".equals(v.getPropertyPath().toString())));
    }

    @Test
    void usernameMuyCorto_violacionEnUsername() {
        RegisterRequest r = new RegisterRequest("Maria", "maria@test.com", "ab", "Valida123!");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(r);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "username".equals(v.getPropertyPath().toString())));
    }

    @Test
    void passwordDebil_violacionEnPassword() {
        RegisterRequest r = new RegisterRequest("Maria", "maria@test.com", "maria", "123");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(r);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "password".equals(v.getPropertyPath().toString())));
    }
}