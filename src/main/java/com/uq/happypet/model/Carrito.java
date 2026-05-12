package com.uq.happypet.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa el carrito de compras
 * asociado a un usuario dentro del sistema.
 */
@Entity
@Table(name = "carritos")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario propietario del carrito.
     * Se utiliza carga perezosa para optimizar rendimiento.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Indica si el carrito se encuentra activo.
     */
    @Column(nullable = false)
    private boolean activo = true;

    /**
     * Lista de productos agregados al carrito.
     *
     * orphanRemoval permite eliminar automáticamente
     * los items que se desvinculen del carrito.
     */
    @OneToMany(
            mappedBy = "carrito",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ItemCarrito> items = new ArrayList<>();

    public Carrito() {
    }

    public Carrito(Usuario usuario, boolean activo) {
        this.usuario = usuario;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<ItemCarrito> getItems() {
        return items;
    }

    public void setItems(List<ItemCarrito> items) {
        this.items = items;
    }
}