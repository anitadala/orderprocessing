package com.example.orderprocessing.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.orderprocessing.service.OrderService;
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
}

