package com.uq.happypet.api;

import com.uq.happypet.dto.ApiMessageResponse;
import com.uq.happypet.exception.CartEmptyException;
import com.uq.happypet.exception.CartItemNotFoundException;
import com.uq.happypet.exception.ProductNotFoundException;
import com.uq.happypet.exception.ProductOutOfStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private ApiExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler();
    }

    @Test
    void handleProductNotFound_404() {
        ResponseEntity<ApiMessageResponse> r =
                handler.handleProductNotFound(new ProductNotFoundException("missing"));
        assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
        assertEquals("PRODUCT_NOT_FOUND", r.getBody().getError());
    }

    @Test
    void handleCartItemNotFound_404() {
        ResponseEntity<ApiMessageResponse> r =
                handler.handleCartItemNotFound(new CartItemNotFoundException("x"));
        assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
        assertEquals("CART_ITEM_NOT_FOUND", r.getBody().getError());
    }

    @Test
    void handleOutOfStock_409() {
        ResponseEntity<ApiMessageResponse> r =
                handler.handleOutOfStock(new ProductOutOfStockException("no stock"));
        assertEquals(HttpStatus.CONFLICT, r.getStatusCode());
        assertEquals("OUT_OF_STOCK", r.getBody().getError());
    }

    @Test
    void handleCartEmpty_400() {
        ResponseEntity<ApiMessageResponse> r =
                handler.handleCartEmpty(new CartEmptyException("empty"));
        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
        assertEquals("CART_EMPTY", r.getBody().getError());
    }

    @Test
    void handleIllegalArgument_400() {
        ResponseEntity<ApiMessageResponse> r =
                handler.handleBadRequest(new IllegalArgumentException("bad"));
        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
        assertEquals("BAD_REQUEST", r.getBody().getError());
    }
}