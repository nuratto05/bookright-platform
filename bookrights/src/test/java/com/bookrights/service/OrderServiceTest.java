package com.bookrights.service;

import com.bookrights.Dto.OrderRequest;
import com.bookrights.enums.OrderStatus;
import com.bookrights.model.Book;
import com.bookrights.model.CustomUserDetails;
import com.bookrights.model.Order;
import com.bookrights.model.User;
import com.bookrights.model.Wallet;
import com.bookrights.repository.BookRepository;
import com.bookrights.repository.OrderRepository;
import com.bookrights.repository.UserRepository;
import com.bookrights.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

class OrderServiceTest {

    private BookRepository bookRepository;
    private UserRepository userRepository;
    private WalletRepository walletRepository;
    private OrderRepository orderRepository;
    private CacheManager cacheManager;
    private OrderService service;

    private User buyer;
    private User seller;
    private Book book;
    private CustomUserDetails buyerDetails;
    private CustomUserDetails sellerDetails;

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        userRepository = mock(UserRepository.class);
        walletRepository = mock(WalletRepository.class);
        orderRepository = mock(OrderRepository.class);
        cacheManager = mock(CacheManager.class);
        service = new OrderService(bookRepository, userRepository, walletRepository, orderRepository, cacheManager);

        buyer = new User("buyer", "Buyer", "buyer@test.local", "password");
        buyer.setId(1L);
        seller = new User("seller", "Seller", "seller@test.local", "password");
        seller.setId(2L);
        book = new Book("Author", "Book", "ISBN-1", 100.0, "Test", seller);
        book.setId(10L);
        buyerDetails = new CustomUserDetails(buyer);
        sellerDetails = new CustomUserDetails(seller);
    }

    private OrderRequest requestForBook() {
        OrderRequest request = new OrderRequest();
        request.setBookId(book.getId());
        return request;
    }

    @Test
    void createOrderReturnsExistingPendingOrderWithoutDuplicateDeduction() {
        Order existing = new Order(buyer, seller, book, 100.0, OrderStatus.PENDING, LocalDate.now().atStartOfDay());

        when(bookRepository.findByIdWithLock(10L)).thenReturn(Optional.of(book));
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(orderRepository.findByBuyerIdAndBookIdAndStatus(1L, 10L, OrderStatus.PENDING))
                .thenReturn(Optional.of(existing));

        var response = service.createOrder(buyerDetails, requestForBook());

        assertEquals("PENDING", response.getStatus());
        verify(walletRepository, never()).findByUserIdWithLock(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrderRejectsInsufficientBalanceWithoutMutation() {
        Wallet wallet = new Wallet(buyer, 50.0);

        when(bookRepository.findByIdWithLock(10L)).thenReturn(Optional.of(book));
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(orderRepository.findByBuyerIdAndBookIdAndStatus(1L, 10L, OrderStatus.PENDING))
                .thenReturn(Optional.empty());
        when(walletRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(wallet));

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> service.createOrder(buyerDetails, requestForBook()));

        assertEquals("Insufficient balance in buyer's wallet", error.getMessage());
        assertEquals(50.0, wallet.getBalance());
        assertEquals(0.0, wallet.getPending_balance());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrderReservesFundsAndCreatesPendingOrder() {
        Wallet wallet = new Wallet(buyer, 1000.0);

        when(bookRepository.findByIdWithLock(10L)).thenReturn(Optional.of(book));
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(orderRepository.findByBuyerIdAndBookIdAndStatus(1L, 10L, OrderStatus.PENDING))
                .thenReturn(Optional.empty());
        when(walletRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(wallet));

        var response = service.createOrder(buyerDetails, requestForBook());

        assertEquals("PENDING", response.getStatus());
        assertEquals(900.0, wallet.getBalance());
        assertEquals(100.0, wallet.getPending_balance());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrderRejectsOwnBook() {
        CustomUserDetails sellerDetails = new CustomUserDetails(seller);
        when(bookRepository.findByIdWithLock(10L)).thenReturn(Optional.of(book));
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> service.createOrder(sellerDetails, requestForBook()));

        assertEquals("You cannot purchase your own book", error.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrderRejectsUnavailableBook() {
        book.setAvailable(false);
        when(bookRepository.findByIdWithLock(10L)).thenReturn(Optional.of(book));
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> service.createOrder(buyerDetails, requestForBook()));

        assertEquals("Book is not available for purchase", error.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void acceptCompletesOrderAndRejectsOtherPendingOrdersWithOneRefundEach() {
        Order order = pendingOrder(30L);
        Wallet buyerWallet = walletWithReservedFunds(buyer);
        Wallet sellerWallet = new Wallet(seller, 0.0);
        User otherBuyer = new User("other-buyer", "Other Buyer", "other@test.local", "password");
        otherBuyer.setId(3L);
        Wallet otherBuyerWallet = walletWithReservedFunds(otherBuyer);
        Order other = new Order(otherBuyer, seller, book, 100.0, OrderStatus.PENDING, LocalDate.now().atStartOfDay());
        other.setId(31L);

        when(orderRepository.findByIdWithLock(30L)).thenReturn(Optional.of(order));
        when(bookRepository.findByIdWithLock(10L)).thenReturn(Optional.of(book));
        when(walletRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(buyerWallet));
        when(walletRepository.findByUserIdWithLock(2L)).thenReturn(Optional.of(sellerWallet));
        when(walletRepository.findByUserIdWithLock(3L)).thenReturn(Optional.of(otherBuyerWallet));
        when(orderRepository.findByBookIdAndStatus(10L, OrderStatus.PENDING)).thenReturn(List.of(order, other));

        var response = service.acceptOrder(sellerDetails, 30L);

        assertEquals("COMPLETED", response.getStatus());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals(OrderStatus.REJECTED, other.getStatus());
        assertFalse(book.isAvailable());
        assertEquals(0.0, buyerWallet.getBalance());
        assertEquals(0.0, buyerWallet.getPending_balance());
        assertEquals(100.0, sellerWallet.getBalance());
        assertEquals(0.0, sellerWallet.getPending_balance());
        assertEquals(100.0, otherBuyerWallet.getBalance());
        assertEquals(0.0, otherBuyerWallet.getPending_balance());
        verify(orderRepository).save(order);
    }

    @Test
    void acceptRejectsNonPendingStatusBeforeMutation() {
        Order order = pendingOrder(30L);
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findByIdWithLock(30L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> service.acceptOrder(sellerDetails, 30L));

        verify(bookRepository, never()).findByIdWithLock(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void acceptRejectsUnauthorizedSeller() {
        Order order = pendingOrder(30L);
        User intruder = new User("intruder", "Intruder", "intruder@test.local", "password");
        intruder.setId(99L);
        when(orderRepository.findByIdWithLock(30L)).thenReturn(Optional.of(order));

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> service.acceptOrder(new CustomUserDetails(intruder), 30L));

        assertEquals("You are not authorized to accept this order", error.getMessage());
        verify(bookRepository, never()).findByIdWithLock(any());
    }

    @Test
    void rejectRefundsBuyerOnceAndChangesStatus() {
        Order order = pendingOrder(30L);
        Wallet wallet = walletWithReservedFunds(buyer);
        when(walletRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(wallet));
        when(orderRepository.findByIdWithLock(30L)).thenReturn(Optional.of(order));

        var response = service.rejectOrder(sellerDetails, 30L);

        assertEquals("REJECTED", response.getStatus());
        assertEquals(OrderStatus.REJECTED, order.getStatus());
        assertEquals(100.0, wallet.getBalance());
        assertEquals(0.0, wallet.getPending_balance());
        verify(orderRepository).save(order);
    }

    @Test
    void rejectThenCancelDoesNotRefundTwice() {
        Order order = pendingOrder(30L);
        Wallet wallet = walletWithReservedFunds(buyer);
        when(walletRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(wallet));
        when(orderRepository.findByIdWithLock(30L)).thenReturn(Optional.of(order));

        service.rejectOrder(sellerDetails, 30L);
        assertThrows(IllegalStateException.class,
                () -> service.cancelOrder(buyerDetails, 30L));

        assertEquals(OrderStatus.REJECTED, order.getStatus());
        assertEquals(100.0, wallet.getBalance());
        assertEquals(0.0, wallet.getPending_balance());
        verify(walletRepository, times(1)).findByUserIdWithLock(1L);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelRefundsBuyerOnceAndChangesStatus() {
        Order order = pendingOrder(30L);
        Wallet wallet = walletWithReservedFunds(buyer);
        when(walletRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(wallet));
        when(orderRepository.findByIdWithLock(30L)).thenReturn(Optional.of(order));

        var response = service.cancelOrder(buyerDetails, 30L);
        assertThrows(IllegalStateException.class,
                () -> service.cancelOrder(buyerDetails, 30L));

        assertEquals("CANCELLED", response.getStatus());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(100.0, wallet.getBalance());
        assertEquals(0.0, wallet.getPending_balance());
        verify(walletRepository, times(1)).findByUserIdWithLock(1L);
        verify(orderRepository).save(order);
    }

    @Test
    void rejectRejectsUnauthorizedSeller() {
        Order order = pendingOrder(30L);
        when(orderRepository.findByIdWithLock(30L)).thenReturn(Optional.of(order));

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> service.rejectOrder(new CustomUserDetails(buyer), 30L));

        assertEquals("You are not authorized to reject this order", error.getMessage());
    }

    @Test
    void cancelRejectsUnauthorizedBuyer() {
        Order order = pendingOrder(30L);
        when(orderRepository.findByIdWithLock(30L)).thenReturn(Optional.of(order));

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> service.cancelOrder(sellerDetails, 30L));

        assertEquals("You are not authorized to cancel this order", error.getMessage());
    }

    private Order pendingOrder(Long id) {
        Order order = new Order(buyer, seller, book, 100.0, OrderStatus.PENDING, LocalDate.now().atStartOfDay());
        order.setId(id);
        return order;
    }

    private Wallet walletWithReservedFunds(User user) {
        Wallet wallet = new Wallet(user, 0.0);
        wallet.setPending_balance(100.0);
        return wallet;
    }
}
