package com.uq.happypet.api;

import com.uq.happypet.dto.OrderResponse;
import com.uq.happypet.exception.CartEmptyException;
import com.uq.happypet.exception.PedidoNoEncontradoException;
import com.uq.happypet.exception.ProductNotFoundException;
import com.uq.happypet.exception.ProductOutOfStockException;
import com.uq.happypet.model.PedidoEstado;
import com.uq.happypet.service.OrderService;
import com.uq.happypet.testsupport.OrderApiSecurityTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de capa web (equivalente a supertest/Jest en Node).
 * JUnit 5 + MockMvc + Mockito + {@link WithMockUser}.
 */
@WebMvcTest(controllers = OrderApiController.class)
@Import({OrderApiSecurityTestConfig.class, ApiExceptionHandler.class})
class OrderApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @MockitoBean
    private OrderService orderService;

    @Nested
    @DisplayName("POST /api/orders/checkout")
    class Checkout {

        @Test
        @WithMockUser(username = "cliente")
        void checkout_exitoso_retornaCreated() throws Exception {
            OrderResponse out = new OrderResponse();
            out.setId(15L);
            out.setEstado(PedidoEstado.CREADO);
            out.setTotal(24000.0);
            when(orderService.checkout(eq("cliente"), org.mockito.ArgumentMatchers.any()))
                    .thenReturn(out);

            String body = """
                    {
                      "direccionEnvio":"Calle 10 # 20-30",
                      "metodoPago":"EFECTIVO",
                      "fechaEntregaPreferida":"2026-05-12",
                      "horarioEntrega":"JORNADA_8_18",
                      "facturacionNombre":"Ana",
                      "facturacionApellidos":"Lopez",
                      "facturacionTipoDocumento":"CC",
                      "facturacionDocumento":"123",
                      "facturacionDireccion":"Calle 1",
                      "facturacionEmail":"ana@test.com"
                    }
                    """;

            mockMvc.perform(post("/api/orders/checkout")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(15))
                    .andExpect(jsonPath("$.estado").value("CREADO"));
        }

        @Test
        @WithMockUser(username = "cliente")
        void checkout_datosInvalidos_retornaBadRequest() throws Exception {
            String body = """
                    {
                      "direccionEnvio":"",
                      "metodoPago":null,
                      "facturacionNombre":""
                    }
                    """;

            mockMvc.perform(post("/api/orders/checkout")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        @WithMockUser(username = "cliente")
        void checkout_correoFacturacionInvalido_retornaBadRequest() throws Exception {
            LocalDate fecha = LocalDate.now().plusDays(5);
            String body = String.format("""
                    {
                      "direccionEnvio":"Calle 10",
                      "metodoPago":"EFECTIVO",
                      "fechaEntregaPreferida":"%s",
                      "horarioEntrega":"JORNADA_8_18",
                      "facturacionNombre":"Ana",
                      "facturacionApellidos":"Lopez",
                      "facturacionTipoDocumento":"CC",
                      "facturacionDocumento":"123",
                      "facturacionDireccion":"Calle 1",
                      "facturacionEmail":"no-es-correo"
                    }
                    """, fecha);

            mockMvc.perform(post("/api/orders/checkout")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

            verify(orderService, never()).checkout(anyString(), any());
        }

        @Test
        @WithMockUser(username = "cliente")
        void checkout_carritoVacio_retornaBadRequestCartEmpty() throws Exception {
            when(orderService.checkout(eq("cliente"), any()))
                    .thenThrow(new CartEmptyException("El carrito esta vacio."));

            mockMvc.perform(post("/api/orders/checkout")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(checkoutJsonVentanaValida()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("CART_EMPTY"));

            verify(orderService).checkout(eq("cliente"), any());
        }

        @Test
        @WithMockUser(username = "cliente")
        void checkout_fechaFueraVentana_retornaBadRequest() throws Exception {
            when(orderService.checkout(eq("cliente"), any()))
                    .thenThrow(new IllegalArgumentException("El dia de entrega debe estar entre ..."));

            mockMvc.perform(post("/api/orders/checkout")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(checkoutJsonVentanaValida()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("BAD_REQUEST"));

            verify(orderService).checkout(eq("cliente"), any());
        }

        @Test
        @WithMockUser(username = "cliente")
        void checkout_sinStock_retornaConflictOutOfStock() throws Exception {
            when(orderService.checkout(eq("cliente"), any()))
                    .thenThrow(new ProductOutOfStockException("Stock insuficiente"));

            mockMvc.perform(post("/api/orders/checkout")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(checkoutJsonVentanaValida()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("OUT_OF_STOCK"));

            verify(orderService).checkout(eq("cliente"), any());
        }

        @Test
        @WithMockUser(username = "cliente")
        void checkout_productoNoEncontrado_retornaNotFound() throws Exception {
            when(orderService.checkout(eq("cliente"), any()))
                    .thenThrow(new ProductNotFoundException("Producto no encontrado."));

            mockMvc.perform(post("/api/orders/checkout")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(checkoutJsonVentanaValida()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"));

            verify(orderService).checkout(eq("cliente"), any());
        }
    }

    private static String checkoutJsonVentanaValida() {
        LocalDate fecha = LocalDate.now().plusDays(5);
        return String.format("""
                {
                  "direccionEnvio":"Calle 10 # 20-30",
                  "metodoPago":"EFECTIVO",
                  "fechaEntregaPreferida":"%s",
                  "horarioEntrega":"JORNADA_8_18",
                  "facturacionNombre":"Ana",
                  "facturacionApellidos":"Lopez",
                  "facturacionTipoDocumento":"CC",
                  "facturacionDocumento":"123",
                  "facturacionDireccion":"Calle 1",
                  "facturacionEmail":"ana@test.com"
                }
                """, fecha);
    }

    @Nested
    @DisplayName("PATCH /api/orders/{id}/status")
    class ActualizarEstado {

        @Test
        @WithMockUser(username = "superadmin", roles = "ADMIN")
        void admin_actualizaCorrectamente() throws Exception {
            OrderResponse out = new OrderResponse();
            out.setId(7L);
            out.setEstado(PedidoEstado.ENVIADO);
            out.setTotal(120.5);
            out.setFecha(LocalDateTime.of(2026, 5, 1, 12, 0));
            when(orderService.actualizarEstadoPedidoAdministracion(eq(7L), eq("enviado")))
                    .thenReturn(out);

            mockMvc.perform(patch("/api/orders/7/status")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(java.util.Map.of("estado", "enviado"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(7))
                    .andExpect(jsonPath("$.estado").value("ENVIADO"));

            verify(orderService).actualizarEstadoPedidoAdministracion(7L, "enviado");
        }

        @Test
        @WithMockUser(username = "superadmin", roles = "ADMIN")
        void admin_actualizaAEntregado_retornaOk() throws Exception {
            OrderResponse out = new OrderResponse();
            out.setId(8L);
            out.setEstado(PedidoEstado.ENTREGADO);
            when(orderService.actualizarEstadoPedidoAdministracion(eq(8L), eq("entregado")))
                    .thenReturn(out);

            mockMvc.perform(patch("/api/orders/8/status")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"estado\":\"entregado\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("ENTREGADO"));

            verify(orderService).actualizarEstadoPedidoAdministracion(8L, "entregado");
        }

        @Test
        @WithMockUser(username = "superadmin", roles = "ADMIN")
        void admin_estadoApiMayusculas_validacionPermite_retornaOk() throws Exception {
            OrderResponse out = new OrderResponse();
            out.setId(9L);
            out.setEstado(PedidoEstado.CONFIRMADO);
            when(orderService.actualizarEstadoPedidoAdministracion(eq(9L), eq("EN_PROCESO")))
                    .thenReturn(out);

            mockMvc.perform(patch("/api/orders/9/status")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"estado\":\"EN_PROCESO\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("CONFIRMADO"));

            verify(orderService).actualizarEstadoPedidoAdministracion(9L, "EN_PROCESO");
        }

        @Test
        @WithMockUser(username = "superadmin", roles = "ADMIN")
        void estadoVacio_retornaValidationError() throws Exception {
            mockMvc.perform(patch("/api/orders/7/status")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"estado\":\"\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

            verify(orderService, never()).actualizarEstadoPedidoAdministracion(anyLong(), anyString());
        }

        @Test
        @WithMockUser(username = "cliente", roles = "USER")
        void clienteSinAdmin_recibeForbidden() throws Exception {
            mockMvc.perform(patch("/api/orders/7/status")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"estado\":\"enviado\"}"))
                    .andExpect(status().isForbidden());

            verify(orderService, never()).actualizarEstadoPedidoAdministracion(anyLong(), anyString());
        }

        @Test
        @WithMockUser(username = "superadmin", roles = "ADMIN")
        void pedidoInexistente_devuelveOrderNotFound() throws Exception {
            when(orderService.actualizarEstadoPedidoAdministracion(eq(404L), anyString()))
                    .thenThrow(new PedidoNoEncontradoException("Pedido no encontrado."));

            mockMvc.perform(patch("/api/orders/404/status")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"estado\":\"entregado\"}"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"));

            verify(orderService).actualizarEstadoPedidoAdministracion(404L, "entregado");
        }

        @Test
        @WithMockUser(username = "superadmin", roles = "ADMIN")
        void estadoNoPermitido_retornaBadRequest() throws Exception {
            mockMvc.perform(patch("/api/orders/7/status")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"estado\":\"cancelado\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

            verify(orderService, never()).actualizarEstadoPedidoAdministracion(anyLong(), anyString());
        }
    }

    @Nested
    @DisplayName("POST /api/orders/{id}/confirmacion-entrega")
    class ConfirmacionEntrega {

        @Test
        @WithMockUser(username = "luis")
        void usuarioAutenticado_confirmaRecepcion() throws Exception {
            OrderResponse out = new OrderResponse();
            out.setId(3L);
            out.setEstado(PedidoEstado.ENTREGADO);
            out.setFechaConfirmacionEntrega(LocalDateTime.of(2026, 5, 5, 15, 30));
            when(orderService.confirmarEntregaCliente(eq("luis"), eq(3L))).thenReturn(out);

            mockMvc.perform(post("/api/orders/3/confirmacion-entrega").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(3))
                    .andExpect(jsonPath("$.estado").value("ENTREGADO"))
                    .andExpect(jsonPath("$.fechaConfirmacionEntrega").exists());

            verify(orderService).confirmarEntregaCliente("luis", 3L);
        }

        @Test
        void sinAutenticacion_noDevuelve200() throws Exception {
            mockMvc.perform(post("/api/orders/3/confirmacion-entrega").with(csrf()))
                    .andExpect(result -> {
                        int sc = result.getResponse().getStatus();
                        org.junit.jupiter.api.Assertions.assertNotEquals(200, sc);
                    });
        }

        @Test
        @WithMockUser(username = "luis")
        void pedidoInexistente_retornaOrderNotFound() throws Exception {
            when(orderService.confirmarEntregaCliente(eq("luis"), eq(404L)))
                    .thenThrow(new PedidoNoEncontradoException("Pedido no encontrado."));

            mockMvc.perform(post("/api/orders/404/confirmacion-entrega").with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"));

            verify(orderService).confirmarEntregaCliente("luis", 404L);
        }

        @Test
        @WithMockUser(username = "luis")
        void pedidoAunNoEntregado_retornaBadRequest() throws Exception {
            when(orderService.confirmarEntregaCliente(eq("luis"), eq(5L)))
                    .thenThrow(new IllegalStateException(
                            "Solo puede confirmarse la recepcion cuando el pedido esta ENTREGADO."));

            mockMvc.perform(post("/api/orders/5/confirmacion-entrega").with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("BAD_REQUEST"));

            verify(orderService).confirmarEntregaCliente("luis", 5L);
        }
    }
}
