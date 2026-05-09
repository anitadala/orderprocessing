package com.example.orderprocessing.exception;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {
	private Instant timestamp;
	private int status;
	private String error;
	private String message;
	private String path;
}

