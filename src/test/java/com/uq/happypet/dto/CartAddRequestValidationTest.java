package com.uq.happypet.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CartAddRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    @Test
    void cantidadCero_fallaMin() {
        CartAddRequest r = new CartAddRequest();
        r.setProductoId(1L);
        r.setCantidad(0);

        Set<ConstraintViolation<CartAddRequest>> v = validator.validate(r);
        assertFalse(v.isEmpty());
    }

    @Test
    void valoresValidos_pasan() {
        CartAddRequest r = new CartAddRequest();
        r.setProductoId(1L);
        r.setCantidad(1);

        assertTrue(validator.validate(r).isEmpty());
    }
}
