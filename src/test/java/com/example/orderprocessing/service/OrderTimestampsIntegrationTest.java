package com.example.orderprocessing.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.orderprocessing.dto.CreateOrderRequest;
import com.example.orderprocessing.dto.OrderItemRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderTimestampsIntegrationTest {

	@Autowired
	private OrderService orderService;

	@Test
	void createdAtAndUpdatedAt_arePopulated() {
		var response = orderService.createOrder(
			CreateOrderRequest.builder()
				.items(List.of(OrderItemRequest.builder().productId("P1").quantity(1).price(10.0).build()))
				.build()
		);

		assertThat(response.getCreatedAt()).isNotNull();
		assertThat(response.getUpdatedAt()).isNotNull();
	}
}

