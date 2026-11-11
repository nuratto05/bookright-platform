package com.bookrights.service;

import com.bookrights.model.Order;
import com.bookrights.Dto.OrderRequest;
import com.bookrights.Dto.OrderResponse;
import com.bookrights.model.Book;
import com.bookrights.model.CustomUserDetails;
import com.bookrights.model.User;
import com.bookrights.model.Wallet;
import com.bookrights.enums.OrderStatus;
import java.time.LocalDateTime;

import com.bookrights.repository.BookRepository;
import com.bookrights.repository.UserRepository;
import com.bookrights.repository.WalletRepository;
import com.bookrights.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class OrderService {
    
    private BookRepository bookRepository;
	private UserRepository userRepository;
	private WalletRepository walletRepository;
    private OrderRepository orderRepository;

    public OrderService(BookRepository bookRepository, UserRepository userRepository, WalletRepository walletRepository, OrderRepository orderRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse createOrder(CustomUserDetails userDetails, OrderRequest orderRequest) {
        Book book = bookRepository.findByIdWithLock(orderRequest.getBookId()).orElseThrow(() -> new NoSuchElementException("Book not found"));
        
        User seller = userRepository.findById(book.getUser().getId()).orElseThrow(() -> new NoSuchElementException("Seller not found"));
        User buyer = userRepository.findById(userDetails.getUser().getId()).orElseThrow(() -> new NoSuchElementException("Buyer not found"));

        if (buyer.getId().equals(seller.getId())) {
            throw new RuntimeException("You cannot purchase your own book");
        }

        if (!book.isAvailable()) {
            throw new RuntimeException("Book is not available for purchase");
        }

        Optional<Order> existingOrder = orderRepository.findByBuyerIdAndBookIdAndStatus(buyer.getId(), book.getId(), OrderStatus.PENDING);

        if(existingOrder.isPresent()) {
            return new OrderResponse(existingOrder.get());
        }
        
        Wallet buyerWallet = walletRepository.findByUserIdWithLock(buyer.getId()).orElseThrow(() -> new NoSuchElementException("Buyer's wallet not found"));

        if(buyerWallet.getBalance() < book.getPrice()) {
            throw new RuntimeException("Insufficient balance in buyer's wallet");
        }

        buyerWallet.setBalance(buyerWallet.getBalance() - book.getPrice());
        buyerWallet.setPending_balance(buyerWallet.getPending_balance() + book.getPrice());

        Order order = new Order(buyer, seller, book, book.getPrice(), OrderStatus.PENDING, LocalDateTime.now());
        orderRepository.save(order);

        return new OrderResponse(order);
    }

    @Transactional
    public OrderResponse acceptOrder(CustomUserDetails userDetails, Long orderId) {
        Order order = orderRepository.findByIdWithLock(orderId).orElseThrow(() -> new NoSuchElementException("Order not found"));

        if(order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Invalid Order Status");
        }
        
        if(!order.getSeller().getId().equals(userDetails.getUser().getId())) {
            throw new RuntimeException("You are not authorized to accept this order");
        }

        Book book = bookRepository.findByIdWithLock(order.getBook().getId()).orElseThrow(() -> new NoSuchElementException("Book not found"));

        if(!book.isAvailable()){
            throw new RuntimeException("Book is not available for purchase");
        }

        Wallet buyerWallet = walletRepository.findByUserIdWithLock(order.getBuyer().getId()).orElseThrow(() -> new NoSuchElementException("Buyer's wallet not found"));
        Wallet sellerWallet = walletRepository.findByUserIdWithLock(order.getSeller().getId()).orElseThrow(() -> new NoSuchElementException("Seller's wallet not found"));
        
        
        buyerWallet.setPending_balance(buyerWallet.getPending_balance() - order.getAmount());
        sellerWallet.setBalance(sellerWallet.getBalance() + order.getAmount());

        book.setAvailable(false);

        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());

        List<Order> orders = orderRepository.findByBookIdAndStatus(book.getId(), OrderStatus.PENDING);
        for(Order otherOrders : orders){
            if(!otherOrders.getId().equals(order.getId())){
                refundBuyer(otherOrders);
                otherOrders.setStatus(OrderStatus.REJECTED);
                otherOrders.setUpdatedAt(LocalDateTime.now());
            }
        }

        orderRepository.save(order);
        return new OrderResponse(order);
    }

    @Transactional
    public OrderResponse rejectOrder(CustomUserDetails userDetails, Long orderId) {
        Order order = orderRepository.findByIdWithLock(orderId).orElseThrow(() -> new NoSuchElementException("Order not found"));

        if(order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be rejected");
        }
        
        if(!order.getSeller().getId().equals(userDetails.getUser().getId())) {
            throw new RuntimeException("You are not authorized to reject this order");
        }

        refundBuyer(order);

        order.setStatus(OrderStatus.REJECTED);
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
        return new OrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(CustomUserDetails userDetails, Long orderId) {
        Order order = orderRepository.findByIdWithLock(orderId).orElseThrow(() -> new NoSuchElementException("Order not found"));

        if(order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be cancelled");
        }
        
        if(!order.getBuyer().getId().equals(userDetails.getUser().getId())) {
            throw new RuntimeException("You are not authorized to cancel this order");
        }

        refundBuyer(order);

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
        return new OrderResponse(order);
    }

    public Page<OrderResponse> getMyPurchases(CustomUserDetails userDetails, Pageable pageable) {
        Page<Order> orders = orderRepository.findByBuyerId(userDetails.getUser().getId(), pageable);
        return orders.map(order -> new OrderResponse(order));
    }

    public Page<OrderResponse> getMySales(CustomUserDetails userDetails, Pageable pageable) {
        Page<Order> orders = orderRepository.findBySellerId(userDetails.getUser().getId(), pageable);
        return orders.map(order -> new OrderResponse(order));
    }

    private void refundBuyer(Order order) {
        Wallet buyerWallet = walletRepository.findByUserIdWithLock(order.getBuyer().getId()).orElseThrow(() -> new NoSuchElementException("Buyer's wallet not found"));
        buyerWallet.setPending_balance(buyerWallet.getPending_balance() - order.getAmount());
        buyerWallet.setBalance(buyerWallet.getBalance() + order.getAmount());
    }
    

}
