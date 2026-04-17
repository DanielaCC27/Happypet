package com.uq.happypet.exception;

public class CartEmptyException extends RuntimeException {

    public CartEmptyException(String message) {
        super(message);
    }
}