package com.uq.happypet.exception;

public class ProductOutOfStockException extends RuntimeException {

    public ProductOutOfStockException(String message) {
        super(message);
    }
}