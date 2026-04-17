package com.uq.happypet.dto;

import java.util.ArrayList;
import java.util.List;

public class CartResponse {

    private Long carritoId;
    private List<CartItemResponse> items = new ArrayList<>();
    private double total;

    public CartResponse() {
    }

    public Long getCarritoId() {
        return carritoId;
    }

    public void setCarritoId(Long carritoId) {
        this.carritoId = carritoId;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}