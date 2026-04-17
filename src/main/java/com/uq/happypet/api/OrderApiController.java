package com.uq.happypet.api;

import com.uq.happypet.dto.CheckoutRequest;
import com.uq.happypet.dto.OrderResponse;
import com.uq.happypet.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(Principal principal,
                                                  @Valid @RequestBody CheckoutRequest body) {
        OrderResponse response = orderService.checkout(principal.getName(), body);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}