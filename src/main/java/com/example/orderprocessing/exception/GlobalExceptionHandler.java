package com.example.orderprocessing.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ResponseEntity<ApiErrorResponse> handleOptimisticLock(
		ObjectOptimisticLockingFailureException ex,
		HttpServletRequest request
	) {
		return build(HttpStatus.CONFLICT, ex, request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ValidationErrorResponse> handleValidationError(
		MethodArgumentNotValidException ex,
		HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

		ValidationErrorResponse body = ValidationErrorResponse.builder()
			.timestamp(Instant.now())
			.status(status.value())
			.error(status.getReasonPhrase())
			.message("Validation failed")
			.path(request.getRequestURI())
			.fieldErrors(fieldErrors)
			.build();

		return ResponseEntity.status(status).body(body);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleBadRequest(
		MethodArgumentTypeMismatchException ex,
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

