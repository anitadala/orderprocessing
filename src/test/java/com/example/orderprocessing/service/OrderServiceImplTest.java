package com.example.orderprocessing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.orderprocessing.dto.CreateOrderRequest;
import com.example.orderprocessing.dto.OrderItemRequest;
import com.example.orderprocessing.exception.InvalidOrderStateException;
import com.example.orderprocessing.exception.OrderNotFoundException;
import com.example.orderprocessing.model.Order;
import com.example.orderprocessing.model.OrderItem;
import com.example.orderprocessing.model.OrderStatus;
import com.example.orderprocessing.repository.OrderRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private OrderServiceImpl orderService;

	@Test
	void createOrder_successfully() {
		CreateOrderRequest request = CreateOrderRequest.builder()
			.items(
				List.of(
					OrderItemRequest.builder().productId("P1").quantity(2).price(10.5).build(),
					OrderItemRequest.builder().productId("P2").quantity(1).price(99.99).build()
				)
			)
			.build();

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
			Order o = invocation.getArgument(0);
			o.setId(1L);
			long itemId = 10L;
			for (OrderItem item : o.getItems()) {
				item.setId(itemId++);
			}
			return o;
		});

		var response = orderService.createOrder(request);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
		assertThat(response.getItems()).hasSize(2);
		assertThat(response.getItems().get(0).getProductId()).isEqualTo("P1");
		assertThat(response.getItems().get(0).getPrice()).isEqualTo(10.5);
	}

	@Test
	void cancel_pendingOrder() {
		Order order = Order.builder().id(5L).status(OrderStatus.PENDING).build();
		when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		orderService.cancelOrder(5L);

		assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
		verify(orderRepository).save(order);
	}

	@Test
	void failToCancel_nonPendingOrder() {
		Order order = Order.builder().id(6L).status(OrderStatus.SHIPPED).build();
		when(orderRepository.findById(6L)).thenReturn(Optional.of(order));

		assertThatThrownBy(() -> orderService.cancelOrder(6L))
			.isInstanceOf(InvalidOrderStateException.class);
	}

	@Test
	void fetchOrdersByStatus() {
		when(orderRepository.findByStatus(OrderStatus.PENDING))
			.thenReturn(List.of(Order.builder().id(1L).status(OrderStatus.PENDING).build()));

		var responses = orderService.getOrdersByStatus(OrderStatus.PENDING);

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
	}

	@Test
	void fetchOrdersByStatus_whenNoneExist_returnsEmptyList() {
		when(orderRepository.findByStatus(OrderStatus.CANCELLED)).thenReturn(List.of());

		var responses = orderService.getOrdersByStatus(OrderStatus.CANCELLED);

		assertThat(responses).isEmpty();
	}

	@Test
	void fetchExistingOrderById_success() {
		Order order = Order.builder()
			.id(42L)
			.status(OrderStatus.PENDING)
			.build();
		when(orderRepository.findById(42L)).thenReturn(Optional.of(order));

		var response = orderService.getOrderById(42L);

		assertThat(response.getId()).isEqualTo(42L);
		assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
	}

	@Test
	void fetchNonExistingOrder_throwsOrderNotFoundException() {
		when(orderRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orderService.getOrderById(999L))
			.isInstanceOf(OrderNotFoundException.class);
	}

	@Test
	void schedulerUpdatesPendingToProcessing() {
		Order o1 = Order.builder().id(1L).status(OrderStatus.PENDING).build();
		Order o2 = Order.builder().id(2L).status(OrderStatus.PENDING).build();
		when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(o1, o2));

		int updatedCount = orderService.processPendingOrders();

		assertThat(updatedCount).isEqualTo(2);
		assertThat(o1.getStatus()).isEqualTo(OrderStatus.PROCESSING);
		assertThat(o2.getStatus()).isEqualTo(OrderStatus.PROCESSING);

		verify(orderRepository).saveAll(List.of(o1, o2));
	}
}

