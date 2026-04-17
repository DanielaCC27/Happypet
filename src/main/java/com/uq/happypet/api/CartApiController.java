package com.uq.happypet.api;

import com.uq.happypet.dto.CartAddRequest;
import com.uq.happypet.dto.CartResponse;
import com.uq.happypet.dto.CartUpdateRequest;
import com.uq.happypet.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    private final CartService cartService;

    public CartApiController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Principal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getName()));
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> add(Principal principal, @Valid @RequestBody CartAddRequest request) {
        return ResponseEntity.ok(cartService.add(principal.getName(), request));
    }

    @PutMapping("/update")
    public ResponseEntity<CartResponse> update(Principal principal, @Valid @RequestBody CartUpdateRequest request) {
        return ResponseEntity.ok(cartService.updateQuantity(principal.getName(), request));
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<CartResponse> remove(Principal principal, @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(principal.getName(), itemId));
    }
}