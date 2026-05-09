package com.example.orderprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OrderprocessingApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderprocessingApplication.class, args);
	}

}
