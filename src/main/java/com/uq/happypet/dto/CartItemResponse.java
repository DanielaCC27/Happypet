package com.uq.happypet.dto;

public class CartItemResponse {

    private Long id;
    private Long productoId;
    private String nombre;
    private String imagenUrl;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public CartItemResponse() {
    }

    public CartItemResponse(Long id, Long productoId, String nombre, String imagenUrl, int cantidad,
                            double precioUnitario, double subtotal) {
        this.id = id;
        this.productoId = productoId;
        this.nombre = nombre;
        this.imagenUrl = imagenUrl;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}