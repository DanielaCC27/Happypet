package com.uq.happypet.dto;

import com.uq.happypet.model.HorarioEntrega;
import com.uq.happypet.model.MetodoPago;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutRequestValidationTest {

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
    void requestVacio_fallaValidacion() {
        CheckoutRequest r = new CheckoutRequest();
        Set<ConstraintViolation<CheckoutRequest>> v = validator.validate(r);
        assertFalse(v.isEmpty());
    }

    @Test
    void requestCompleto_pasa() {
        CheckoutRequest r = new CheckoutRequest();
        r.setDireccionEnvio("Calle 10");
        r.setMetodoPago(MetodoPago.TRANSFERENCIA);
        r.setFechaEntregaPreferida(LocalDate.now().plusDays(1));
        r.setHorarioEntrega(HorarioEntrega.JORNADA_8_18);
        r.setFacturacionNombre("Nombre");
        r.setFacturacionApellidos("Apellido");
        r.setFacturacionTipoDocumento("CC");
        r.setFacturacionDocumento("123456");
        r.setFacturacionDireccion("Dir");
        r.setFacturacionEmail("ok@example.com");

        Set<ConstraintViolation<CheckoutRequest>> v = validator.validate(r);
        assertTrue(v.isEmpty(), () -> v.toString());
    }

    @Test
    void emailInvalido_falla() {
        CheckoutRequest r = modeloValido();
        r.setFacturacionEmail("no-es-correo");

        Set<ConstraintViolation<CheckoutRequest>> v = validator.validate(r);
        assertFalse(v.isEmpty());
    }

    private static CheckoutRequest modeloValido() {
        CheckoutRequest r = new CheckoutRequest();
        r.setDireccionEnvio("C");
        r.setMetodoPago(MetodoPago.EFECTIVO);
        r.setFechaEntregaPreferida(LocalDate.now());
        r.setHorarioEntrega(HorarioEntrega.JORNADA_8_18);
        r.setFacturacionNombre("N");
        r.setFacturacionApellidos("A");
        r.setFacturacionTipoDocumento("CC");
        r.setFacturacionDocumento("1");
        r.setFacturacionDireccion("D");
        r.setFacturacionEmail("a@b.co");
        return r;
    }
}
