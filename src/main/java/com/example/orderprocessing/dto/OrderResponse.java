package com.example.orderprocessing.dto;

import com.example.orderprocessing.model.OrderStatus;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
	private Long id;
	private OrderStatus status;
	private Instant createdAt;
	private Instant updatedAt;
	private List<OrderItemResponse> items;
}

