package com.bookrights.Dto;

import com.bookrights.model.Order;

public class OrderResponse {
    
    private Long orderId;
    private Long buyerId;
    private Long sellerId;
    private Long bookId;
    private String bookIsbn;
    private String status;
    private double price;

    public OrderResponse(Order order) {
        this.orderId = order.getId();
        this.buyerId = order.getBuyer().getId();
        this.sellerId = order.getSeller().getId();
        this.bookId = order.getBook().getId();
        this.bookIsbn = order.getBook().getIsbn();
        this.status = order.getStatus().toString();
        this.price = order.getAmount();
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public Long getBookId() {
        return bookId;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public String getStatus() {
        return status;
    }

    public double getPrice() {
        return price;
    }

}
