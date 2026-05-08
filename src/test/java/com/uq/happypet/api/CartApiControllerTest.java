package com.uq.happypet.api;

import com.uq.happypet.dto.CartAddRequest;
import com.uq.happypet.dto.CartItemResponse;
import com.uq.happypet.dto.CartResponse;
import com.uq.happypet.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartApiController.class)
class CartApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @MockitoBean
    private CartService cartService;

    @Test
    @WithMockUser(username = "maria")
    void getCart_200() throws Exception {
        CartResponse dto = new CartResponse();
        dto.setTotal(0);
        when(cartService.getCart("maria")).thenReturn(dto);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));

        verify(cartService).getCart("maria");
    }

    @Test
    @WithMockUser(username = "carlos")
    void getCart_vacio_itemsArrayVacioYTotalCero() throws Exception {
        CartResponse dto = new CartResponse();
        dto.setCarritoId(null);
        dto.setItems(List.of());
        dto.setTotal(0);
        when(cartService.getCart("carlos")).thenReturn(dto);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());

        verify(cartService).getCart("carlos");
    }

    @Test
    void getCart_sinAutenticacion_noDevuelve200() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(result -> {
                    int sc = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertNotEquals(200, sc);
                });
    }

    @Test
    void add_conUsuario() throws Exception {
        CartAddRequest body = new CartAddRequest();
        body.setProductoId(1L);
        body.setCantidad(1);

        CartResponse out = new CartResponse();
        when(cartService.add(eq("juan"), any(CartAddRequest.class))).thenReturn(out);

        mockMvc.perform(post("/api/cart/add")
                        .with(user("juan"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(cartService).add(eq("juan"), any(CartAddRequest.class));
    }

    @Test
    void add_respuestaIncluyeTotalYLineas() throws Exception {
        CartAddRequest body = new CartAddRequest();
        body.setProductoId(5L);
        body.setCantidad(2);

        CartResponse out = new CartResponse();
        out.setCarritoId(7L);
        out.setItems(List.of(new CartItemResponse(10L, 5L, "Collar", "c.jpg", 2, 9000, 18000)));
        out.setTotal(18000);
        when(cartService.add(eq("juan"), any(CartAddRequest.class))).thenReturn(out);

        mockMvc.perform(post("/api/cart/add")
                        .with(user("juan"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(18000))
                .andExpect(jsonPath("$.carritoId").value(7))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].subtotal").value(18000))
                .andExpect(jsonPath("$.items[0].cantidad").value(2));

        verify(cartService).add(eq("juan"), any(CartAddRequest.class));
    }

    @Test
    void add_payloadInvalido_retornaBadRequest() throws Exception {
        String invalido = "{\"productoId\":1,\"cantidad\":0}";

        mockMvc.perform(post("/api/cart/add")
                        .with(user("juan"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(cartService, never()).add(eq("juan"), any(CartAddRequest.class));
    }

    @Test
    void update_conUsuario() throws Exception {
        String json = "{\"itemId\":9,\"cantidad\":2}";
        when(cartService.updateQuantity(eq("ana"), any())).thenReturn(new CartResponse());

        mockMvc.perform(put("/api/cart/update")
                        .with(user("ana"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void remove_conPathVariable() throws Exception {
        when(cartService.removeItem("pepe", 12L)).thenReturn(new CartResponse());

        mockMvc.perform(delete("/api/cart/remove/{itemId}", 12L).with(user("pepe")).with(csrf()))
                .andExpect(status().isOk());

        verify(cartService).removeItem("pepe", 12L);
    }

    @Test
    void remove_carritoQuedaVacio_totalCero() throws Exception {
        CartResponse vacio = new CartResponse();
        vacio.setCarritoId(null);
        vacio.setItems(List.of());
        vacio.setTotal(0);
        when(cartService.removeItem("pepe", 12L)).thenReturn(vacio);

        mockMvc.perform(delete("/api/cart/remove/{itemId}", 12L).with(user("pepe")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.items").isEmpty());

        verify(cartService).removeItem("pepe", 12L);
    }
}
