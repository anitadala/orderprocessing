package com.example.orderprocessing.dto;

import com.example.orderprocessing.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {
	@NotNull(message = "status must not be null")
	private OrderStatus status;
}

