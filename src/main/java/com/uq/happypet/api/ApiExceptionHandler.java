package com.uq.happypet.api;

import com.uq.happypet.dto.ApiMessageResponse;
import com.uq.happypet.exception.CartEmptyException;
import com.uq.happypet.exception.CartItemNotFoundException;
import com.uq.happypet.exception.ProductNotFoundException;
import com.uq.happypet.exception.ProductOutOfStockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.uq.happypet.api")
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiMessageResponse> handleValidation(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        String first = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Datos invalidos");
        ApiMessageResponse body = new ApiMessageResponse(
                "VALIDATION_ERROR",
                first,
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiMessageResponse> handleProductNotFound(ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiMessageResponse("PRODUCT_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ApiMessageResponse> handleCartItemNotFound(CartItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiMessageResponse("CART_ITEM_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<ApiMessageResponse> handleOutOfStock(ProductOutOfStockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiMessageResponse("OUT_OF_STOCK", ex.getMessage()));
    }

    @ExceptionHandler(CartEmptyException.class)
    public ResponseEntity<ApiMessageResponse> handleCartEmpty(CartEmptyException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiMessageResponse("CART_EMPTY", ex.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiMessageResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiMessageResponse("BAD_REQUEST", ex.getMessage()));
    }
}