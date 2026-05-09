package com.example.orderprocessing.service;

import com.example.orderprocessing.dto.CreateOrderRequest;
import com.example.orderprocessing.dto.OrderResponse;
import com.example.orderprocessing.model.OrderStatus;
import java.util.List;

public interface OrderService {
	OrderResponse createOrder(CreateOrderRequest request);

	OrderResponse getOrderById(Long id);

	List<OrderResponse> getOrders(OrderStatus status);

	void cancelOrder(Long id);

	OrderResponse updateOrderStatus(Long id, OrderStatus status);

	/**
	 * Scheduler-facing operation: move all PENDING orders to PROCESSING.
	 */
	int processPendingOrders();
}
