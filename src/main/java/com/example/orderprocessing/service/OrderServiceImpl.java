package com.example.orderprocessing.service;

import com.example.orderprocessing.dto.CreateOrderRequest;
import com.example.orderprocessing.dto.OrderItemRequest;
import com.example.orderprocessing.dto.OrderItemResponse;
import com.example.orderprocessing.dto.OrderResponse;
import com.example.orderprocessing.exception.InvalidOrderStateException;
import com.example.orderprocessing.exception.OrderNotFoundException;
import com.example.orderprocessing.model.Order;
import com.example.orderprocessing.model.OrderItem;
import com.example.orderprocessing.model.OrderStatus;
import com.example.orderprocessing.repository.OrderRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;

	@Override
	@Transactional
	public OrderResponse createOrder(CreateOrderRequest request) {
		// Business rule: all orders start in PENDING status.
		Order order = Order.builder()
			.status(OrderStatus.PENDING)
			.build();

		if (request != null && request.getItems() != null) {
			for (OrderItemRequest itemRequest : request.getItems()) {
				order.addItem(toEntity(itemRequest));
			}
		}

		Order saved = orderRepository.save(order);
		return toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public OrderResponse getOrderById(Long id) {
		return toResponse(getOrderOrThrow(id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
		return orderRepository.findByStatus(status).stream().map(this::toResponse).toList();
	}

	@Override
	@Transactional
	public void cancelOrder(Long id) {
		Order order = getOrderOrThrow(id);

		// Business rule: cancel is allowed only when the order is PENDING.
		if (order.getStatus() != OrderStatus.PENDING) {
			throw new InvalidOrderStateException(id, order.getStatus(), "cancel");
		}

		order.setStatus(OrderStatus.CANCELLED);
		orderRepository.save(order);
	}

	@Override
	@Transactional
	public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
		Objects.requireNonNull(status, "status must not be null");
		Order order = getOrderOrThrow(id);
		order.setStatus(status);
		return toResponse(orderRepository.save(order));
	}

	@Override
	@Transactional
	public int processPendingOrders() {
		List<Order> pending = orderRepository.findByStatus(OrderStatus.PENDING);
		for (Order order : pending) {
			order.setStatus(OrderStatus.PROCESSING);
		}
		orderRepository.saveAll(pending);
		return pending.size();
	}

	private Order getOrderOrThrow(Long id) {
		return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
	}

	private OrderItem toEntity(OrderItemRequest request) {
		if (request == null) {
			return OrderItem.builder()
				.productId(null)
				.quantity(0)
				.price(0.0)
				.build();
		}

		// Business rule: price is a snapshot captured into OrderItem at order creation time.
		return OrderItem.builder()
			.productId(request.getProductId())
			.quantity(request.getQuantity())
			.price(request.getPrice())
			.build();
	}

	private OrderResponse toResponse(Order order) {
		return OrderResponse.builder()
			.id(order.getId())
			.status(order.getStatus())
			.createdAt(order.getCreatedAt())
			.updatedAt(order.getUpdatedAt())
			.items(
				order.getItems().stream()
					.map(
						item -> OrderItemResponse.builder()
							.id(item.getId())
							.productId(item.getProductId())
							.quantity(item.getQuantity())
							.price(item.getPrice())
							.build()
					)
					.collect(Collectors.toList())
			)
			.build();
	}
}

