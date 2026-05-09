package com.example.orderprocessing.repository;

import com.example.orderprocessing.model.Order;
import com.example.orderprocessing.model.OrderStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
	List<Order> findByStatus(OrderStatus status);

	@Modifying
	@Query("""
		update Order o
		set o.status = com.example.orderprocessing.model.OrderStatus.PROCESSING,
		    o.updatedAt = :now
		where o.status = com.example.orderprocessing.model.OrderStatus.PENDING
		""")
	int moveAllPendingToProcessing(@Param("now") Instant now);
}

