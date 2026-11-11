package com.bookrights.repository;

import com.bookrights.enums.OrderStatus;
import com.bookrights.model.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;
import jakarta.persistence.LockModeType;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByBuyerIdAndBookIdAndStatus(Long buyerId, Long bookId, OrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query ("SELECT o FROM Order o WHERE o.id = :orderId")
    Optional<Order> findByIdWithLock(@Param("orderId") Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query ("SELECT o FROM Order o WHERE o.book.id = :bookId AND o.status = :status")
    List<Order> findByBookIdAndStatus(@Param("bookId") Long bookId, @Param("status") OrderStatus status);

    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);
    Page<Order> findBySellerId(Long sellerId, Pageable pageable);

}
