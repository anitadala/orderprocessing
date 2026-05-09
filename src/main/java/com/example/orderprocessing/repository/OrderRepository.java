package com.example.orderprocessing.repository;

import com.example.orderprocessing.model.Order;
import com.example.orderprocessing.model.OrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
	List<Order> findByStatus(OrderStatus status);
}

