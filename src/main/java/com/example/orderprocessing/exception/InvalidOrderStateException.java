package com.example.orderprocessing.exception;

import com.example.orderprocessing.model.OrderStatus;

public class InvalidOrderStateException extends RuntimeException {
	public InvalidOrderStateException(Long orderId, OrderStatus currentStatus, String action) {
		super(
			"Cannot " + action + " order " + orderId + " when status is " + currentStatus
		);
	}
}

