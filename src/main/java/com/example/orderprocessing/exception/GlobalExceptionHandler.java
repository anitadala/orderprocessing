package com.example.orderprocessing.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleOrderNotFound(
		OrderNotFoundException ex,
		HttpServletRequest request
	) {
		return build(HttpStatus.NOT_FOUND, ex, request);
	}

	@ExceptionHandler(InvalidOrderStateException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidOrderState(
		InvalidOrderStateException ex,
		HttpServletRequest request
	) {
		return build(HttpStatus.CONFLICT, ex, request);
	}

	@ExceptionHandler({MethodArgumentTypeMismatchException.class, MethodArgumentNotValidException.class})
	public ResponseEntity<ApiErrorResponse> handleBadRequest(
		Exception ex,
		HttpServletRequest request
	) {
		return build(HttpStatus.BAD_REQUEST, ex, request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnhandled(
		Exception ex,
		HttpServletRequest request
	) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
	}

	private ResponseEntity<ApiErrorResponse> build(
		HttpStatus status,
		Exception ex,
		HttpServletRequest request
	) {
		ApiErrorResponse body = ApiErrorResponse.builder()
			.timestamp(Instant.now())
			.status(status.value())
			.error(status.getReasonPhrase())
			.message(ex.getMessage())
			.path(request.getRequestURI())
			.build();

		return ResponseEntity.status(status).body(body);
	}
}

