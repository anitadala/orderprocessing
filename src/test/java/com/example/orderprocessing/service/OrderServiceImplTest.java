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
import org.springframework.orm.ObjectOptimisticLockingFailureException;

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
	void cancel_orderWhenProcessing_fails() {
		Order order = Order.builder().id(6L).status(OrderStatus.PROCESSING).build();
		when(orderRepository.findById(6L)).thenReturn(Optional.of(order));

		assertThatThrownBy(() -> orderService.cancelOrder(6L))
			.isInstanceOf(InvalidOrderStateException.class);
	}

	@Test
	void cancel_orderWhenShipped_fails() {
		Order order = Order.builder().id(7L).status(OrderStatus.SHIPPED).build();
		when(orderRepository.findById(7L)).thenReturn(Optional.of(order));

		assertThatThrownBy(() -> orderService.cancelOrder(7L))
			.isInstanceOf(InvalidOrderStateException.class);
	}

	@Test
	void cancel_orderWhenDelivered_fails() {
		Order order = Order.builder().id(8L).status(OrderStatus.DELIVERED).build();
		when(orderRepository.findById(8L)).thenReturn(Optional.of(order));

		assertThatThrownBy(() -> orderService.cancelOrder(8L))
			.isInstanceOf(InvalidOrderStateException.class);
	}

	@Test
	void cancel_nonExistingOrder_throwsOrderNotFoundException() {
		when(orderRepository.findById(9999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orderService.cancelOrder(9999L))
			.isInstanceOf(OrderNotFoundException.class);
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
	void update_pendingToProcessing_success() {
		Order order = Order.builder().id(100L).status(OrderStatus.PENDING).build();
		when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = orderService.updateOrderStatus(100L, OrderStatus.PROCESSING);

		assertThat(response.getStatus()).isEqualTo(OrderStatus.PROCESSING);
	}

	@Test
	void update_processingToShipped_success() {
		Order order = Order.builder().id(101L).status(OrderStatus.PROCESSING).build();
		when(orderRepository.findById(101L)).thenReturn(Optional.of(order));
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = orderService.updateOrderStatus(101L, OrderStatus.SHIPPED);

		assertThat(response.getStatus()).isEqualTo(OrderStatus.SHIPPED);
	}

	@Test
	void update_shippedToDelivered_success() {
		Order order = Order.builder().id(102L).status(OrderStatus.SHIPPED).build();
		when(orderRepository.findById(102L)).thenReturn(Optional.of(order));
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = orderService.updateOrderStatus(102L, OrderStatus.DELIVERED);

		assertThat(response.getStatus()).isEqualTo(OrderStatus.DELIVERED);
	}

	@Test
	void update_invalidTransition_deliveredToPending_fails() {
		Order order = Order.builder().id(103L).status(OrderStatus.DELIVERED).build();
		when(orderRepository.findById(103L)).thenReturn(Optional.of(order));

		assertThatThrownBy(() -> orderService.updateOrderStatus(103L, OrderStatus.PENDING))
			.isInstanceOf(InvalidOrderStateException.class);
	}

	@Test
	void updateStatus_forNonExistingOrder_throwsOrderNotFoundException() {
		when(orderRepository.findById(8888L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orderService.updateOrderStatus(8888L, OrderStatus.PROCESSING))
			.isInstanceOf(OrderNotFoundException.class);
	}

	@Test
	void schedulerUpdatesPendingToProcessing() {
		when(orderRepository.moveAllPendingToProcessing(any())).thenReturn(2);

		int updatedCount = orderService.processPendingOrders();

		assertThat(updatedCount).isEqualTo(2);
		verify(orderRepository).moveAllPendingToProcessing(any());
	}

	@Test
	void schedulerRunsSafely_whenNoPendingOrdersExist() {
		Order cancelled = Order.builder().id(11L).status(OrderStatus.CANCELLED).build();
		Order processing = Order.builder().id(12L).status(OrderStatus.PROCESSING).build();

		when(orderRepository.moveAllPendingToProcessing(any())).thenReturn(0);

		int updatedCount = orderService.processPendingOrders();

		assertThat(updatedCount).isZero();
		assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
		assertThat(processing.getStatus()).isEqualTo(OrderStatus.PROCESSING);
		verify(orderRepository).moveAllPendingToProcessing(any());
	}

	@Test
	void cancelWhileSchedulerUpdates_onlyOneWins_cancelFirst() {
		Order order = Order.builder().id(200L).status(OrderStatus.PENDING).build();
		when(orderRepository.findById(200L)).thenReturn(Optional.of(order));
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		orderService.cancelOrder(200L); // cancels -> status becomes CANCELLED
		when(orderRepository.moveAllPendingToProcessing(any())).thenReturn(0); // nothing left pending

		int updated = orderService.processPendingOrders();

		assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
		assertThat(updated).isZero();
	}

	@Test
	void cancelWhileSchedulerUpdates_onlyOneWins_schedulerFirst() {
		when(orderRepository.moveAllPendingToProcessing(any())).thenReturn(1);

		int updated = orderService.processPendingOrders();

		Order orderNowProcessing = Order.builder().id(201L).status(OrderStatus.PROCESSING).build();
		when(orderRepository.findById(201L)).thenReturn(Optional.of(orderNowProcessing));

		assertThat(updated).isEqualTo(1);
		assertThatThrownBy(() -> orderService.cancelOrder(201L))
			.isInstanceOf(InvalidOrderStateException.class);
	}

	@Test
	void twoConcurrentStatusUpdates_oneFailsOptimisticLock_finalStateConsistent() {
		Order order = Order.builder().id(300L).status(OrderStatus.PENDING).build();
		when(orderRepository.findById(300L)).thenReturn(Optional.of(order));
		when(orderRepository.save(any(Order.class)))
			.thenAnswer(invocation -> invocation.getArgument(0))
			.thenThrow(new ObjectOptimisticLockingFailureException(Order.class, 300L));

		var first = orderService.updateOrderStatus(300L, OrderStatus.PROCESSING);
		assertThat(first.getStatus()).isEqualTo(OrderStatus.PROCESSING);

		assertThatThrownBy(() -> orderService.updateOrderStatus(300L, OrderStatus.SHIPPED))
			.isInstanceOf(ObjectOptimisticLockingFailureException.class);
	}
}

