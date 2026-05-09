package com.example.orderprocessing.scheduler;

import com.example.orderprocessing.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderProcessingScheduler {

	private final OrderService orderService;

	@Scheduled(fixedDelayString = "PT5M")
	public void movePendingToProcessing() {
		orderService.processPendingOrders();
	}
}

