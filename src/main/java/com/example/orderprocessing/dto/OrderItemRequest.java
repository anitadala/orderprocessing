package com.example.orderprocessing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {
	@NotBlank(message = "productId must not be blank")
	private String productId;

	@Min(value = 1, message = "quantity must be at least 1")
	private int quantity;

	@Positive(message = "price must be > 0")
	private double price;
}

