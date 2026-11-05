package com.bookrights.controller;

import org.springframework.web.bind.annotation.RestController;

import com.bookrights.model.CustomUserDetails;
import com.bookrights.Dto.OrderRequest;
import com.bookrights.Dto.OrderResponse;
import com.bookrights.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController 
public class OrderController {
    

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/transaction/order/placeOrder")
    public ResponseEntity<?> placeOrder(@AuthenticationPrincipal CustomUserDetails user, @RequestBody OrderRequest request) {

        try{
            OrderResponse orderResponse = orderService.createOrder(user, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);

        } catch (RuntimeException e) {

            if (e.getMessage().equals("Book not found") || e.getMessage().equals("Seller not found") || e.getMessage().equals("Buyer not found") || e.getMessage().equals("Buyer's wallet not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }

            if (e.getMessage().equals("Insufficient balance in buyer's wallet") || e.getMessage().equals("You cannot purchase your own book") || e.getMessage().equals("Book is not available for purchase")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @PutMapping("/transaction/order/{orderId}/accept")
    public ResponseEntity<?> acceptOrder(@AuthenticationPrincipal CustomUserDetails user, @PathVariable("orderId") Long orderId) {

        try{
            OrderResponse orderResponse = orderService.acceptOrder(user, orderId);
            return ResponseEntity.ok().body(orderResponse);

        } catch (RuntimeException e) {

            if (e.getMessage().equals("Order not found") || e.getMessage().equals("Buyer's wallet not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }

            if (e.getMessage().equals("You are not authorized to accept this order") || e.getMessage().equals("Book is not available for purchase") || e.getMessage().equals("Invalid Order Status") || e.getMessage().equals("Insufficient funds")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }

    }

    @PutMapping("/transaction/order/{orderId}/reject")
    public ResponseEntity<?> rejectOrder(@AuthenticationPrincipal CustomUserDetails user, @PathVariable("orderId") Long orderId) {

        try{
            OrderResponse orderResponse = orderService.rejectOrder(user, orderId);
            return ResponseEntity.ok().body(orderResponse);

        } catch (RuntimeException e) {

            if (e.getMessage().equals("Order not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }

            if (e.getMessage().equals("You are not authorized to reject this order") || e.getMessage().equals("Invalid Order Status")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }

    }

    @GetMapping("/transaction/order/getMyPurchases")
    public ResponseEntity<?> getMyPurchases(@AuthenticationPrincipal CustomUserDetails user, Pageable pageable) {

        try{
            Page<OrderResponse> orders = orderService.getMyPurchases(user, pageable);
            return ResponseEntity.ok().body(orders);

        } catch (RuntimeException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @GetMapping("/transaction/order/getMySales")
    public ResponseEntity<?> getMySales(@AuthenticationPrincipal CustomUserDetails user, Pageable pageable) {

        try{
            Page<OrderResponse> orders = orderService.getMySales(user, pageable);
            return ResponseEntity.ok().body(orders);

        } catch (RuntimeException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

}
