package com.example.orderprocessing.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.orderprocessing.exception.InvalidOrderStateException;
import com.example.orderprocessing.exception.OrderNotFoundException;
import com.example.orderprocessing.dto.OrderResponse;
import com.example.orderprocessing.model.OrderStatus;
import com.example.orderprocessing.service.OrderService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerValidationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OrderService orderService;

	@Test
	void createOrder_returns400_whenItemsEmpty() throws Exception {
		mockMvc.perform(
				post("/orders")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"items\":[]}")
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors.items").exists());

		verifyNoInteractions(orderService);
	}

	@Test
	void createOrder_returns400_whenItemInvalid() throws Exception {
		mockMvc.perform(
				post("/orders")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"items\":[{\"productId\":\"\",\"quantity\":0,\"price\":-1}]}")
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors['items[0].productId']").exists())
			.andExpect(jsonPath("$.fieldErrors['items[0].quantity']").exists())
			.andExpect(jsonPath("$.fieldErrors['items[0].price']").exists());

		verifyNoInteractions(orderService);
	}

	@Test
	void createOrder_returns400_whenPriceIsZero() throws Exception {
		mockMvc.perform(
				post("/orders")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"items\":[{\"productId\":\"P1\",\"quantity\":1,\"price\":0}]}")
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors['items[0].price']").exists());

		verifyNoInteractions(orderService);
	}

	@Test
	void updateStatus_returns400_whenStatusMissing() throws Exception {
		mockMvc.perform(
				put("/orders/1/status")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{}")
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors.status").exists());

		verifyNoInteractions(orderService);
	}

	@Test
	void getOrders_returns200_whenStatusIsNotProvided() throws Exception {
		when(orderService.getOrders(null))
			.thenReturn(
				List.of(
					OrderResponse.builder().id(1L).status(OrderStatus.PENDING).build(),
					OrderResponse.builder().id(2L).status(OrderStatus.SHIPPED).build()
				)
			);

		mockMvc.perform(get("/orders"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].status").value("PENDING"))
			.andExpect(jsonPath("$[1].id").value(2))
			.andExpect(jsonPath("$[1].status").value("SHIPPED"));

		verify(orderService).getOrders(null);
	}

	@Test
	void getOrders_returns200_whenStatusFilterIsProvided() throws Exception {
		when(orderService.getOrders(OrderStatus.PENDING))
			.thenReturn(List.of(OrderResponse.builder().id(1L).status(OrderStatus.PENDING).build()));

		mockMvc.perform(get("/orders").param("status", "PENDING"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].status").value("PENDING"));

		verify(orderService).getOrders(OrderStatus.PENDING);
	}

	@Test
	void getOrder_returns404WithMeaningfulMessage_whenOrderNotFound() throws Exception {
		when(orderService.getOrderById(99L)).thenThrow(new OrderNotFoundException(99L));

		mockMvc.perform(get("/orders/99"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.error").value("Not Found"))
			.andExpect(jsonPath("$.message").value("Order not found: 99"))
			.andExpect(jsonPath("$.path").value("/orders/99"));
	}

	@Test
	void updateStatus_returns409WithMeaningfulMessage_whenOrderStateInvalid() throws Exception {
		when(orderService.updateOrderStatus(7L, OrderStatus.PROCESSING))
			.thenThrow(new InvalidOrderStateException(7L, OrderStatus.SHIPPED, "transition to PROCESSING"));

		mockMvc.perform(
				put("/orders/7/status")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"status\":\"PROCESSING\"}")
			)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status").value(409))
			.andExpect(jsonPath("$.error").value("Conflict"))
			.andExpect(jsonPath("$.message").value("Cannot transition to PROCESSING order 7 when status is SHIPPED"))
			.andExpect(jsonPath("$.path").value("/orders/7/status"));
	}
}
