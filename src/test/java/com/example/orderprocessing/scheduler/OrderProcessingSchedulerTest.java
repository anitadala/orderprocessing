package com.example.orderprocessing.scheduler;

import static org.mockito.Mockito.verify;

import com.example.orderprocessing.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderProcessingSchedulerTest {

	@Mock
	private OrderService orderService;

	@InjectMocks
	private OrderProcessingScheduler scheduler;

	@Test
	void schedulerInvokesServiceProcessor() {
		scheduler.movePendingToProcessing();
		verify(orderService).processPendingOrders();
	}
}

