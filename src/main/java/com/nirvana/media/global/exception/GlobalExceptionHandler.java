package com.nirvana.media.global.exception;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.nirvana.media.global.response.ApiResponse;
import com.nirvana.media.global.response.ErrorDetail;
import com.nirvana.media.global.response.ErrorResponse;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 모든 예외를 공통 오류 형식(success=false, error.code·message·traceId)으로 바꾼다. (MED-NFR-05)
// Spring MVC 표준 예외(404, 405, 읽을 수 없는 본문 등)는 ResponseEntityExceptionHandler가 정한 상태코드를 그대로 쓰고,
// 본문만 공통 오류 형식으로 바꾼다.
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	// 트레이싱이 꺼진 환경에서도 오류 응답은 나가야 하므로 Tracer 빈이 없어도 되게 받는다.
	private final ObjectProvider<Tracer> tracerProvider;

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		log.warn("Business exception occurred: code={}", errorCode.getCode());
		return createErrorResponse(errorCode);
	}

	// 내부 정보(메시지, SQL, 스택 트레이스)는 로그에만 남기고 응답에는 일반화된 메시지만 준다.
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
		log.error("Unexpected exception occurred", exception);
		return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
	}

	// 요청 DTO @Valid 검증 실패는 필드별 사유를 details에 담는다. 입력값 자체는 응답에 넣지 않는다.
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
		HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
		ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
		List<ErrorDetail> details = exception.getBindingResult().getFieldErrors().stream()
			.map(fieldError -> new ErrorDetail(fieldError.getField(), fieldError.getDefaultMessage()))
			.toList();
		log.warn("Validation failed: fields={}", details.stream().map(ErrorDetail::field).toList());
		ErrorResponse error = new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), details, findTraceId());
		return ResponseEntity
			.status(errorCode.getStatus())
			.headers(headers)
			.body(ApiResponse.failure(error));
	}

	// Spring 예외 메시지(파서 오류 원문, 클래스명)는 응답에 넣지 않고 ErrorCode의 메시지만 쓴다.
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception exception, @Nullable Object body,
		HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
		ErrorCode errorCode = resolveErrorCode(statusCode);
		logSpringException(exception, statusCode);
		return super.handleExceptionInternal(exception, createFailure(errorCode), headers, statusCode, request);
	}

	// 표에 없는 4xx(406, 413, 415 등)는 새 코드를 만들지 않고 상태코드만 유지한 채 INVALID_REQUEST로 응답한다.
	private ErrorCode resolveErrorCode(HttpStatusCode statusCode) {
		if (statusCode.is5xxServerError()) {
			return ErrorCode.INTERNAL_SERVER_ERROR;
		}
		if (statusCode.isSameCodeAs(HttpStatus.NOT_FOUND)) {
			return ErrorCode.NOT_FOUND;
		}
		if (statusCode.isSameCodeAs(HttpStatus.METHOD_NOT_ALLOWED)) {
			return ErrorCode.METHOD_NOT_ALLOWED;
		}
		return ErrorCode.INVALID_REQUEST;
	}

	private void logSpringException(Exception exception, HttpStatusCode statusCode) {
		if (statusCode.is5xxServerError()) {
			log.error("Request failed: status={}", statusCode.value(), exception);
			return;
		}
		log.warn("Invalid request: status={}, exception={}", statusCode.value(), exception.getClass().getSimpleName());
	}

	private ResponseEntity<ApiResponse<Void>> createErrorResponse(ErrorCode errorCode) {
		return ResponseEntity
			.status(errorCode.getStatus())
			.body(createFailure(errorCode));
	}

	private ApiResponse<Void> createFailure(ErrorCode errorCode) {
		return ApiResponse.failure(new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), findTraceId()));
	}

	// Trace ID를 구할 수 없으면 null을 돌려준다. 오류 응답 자체는 실패하지 않는다.
	private @Nullable String findTraceId() {
		Tracer tracer = tracerProvider.getIfAvailable();
		if (tracer == null) {
			return null;
		}
		Span span = tracer.currentSpan();
		if (span == null || !StringUtils.hasText(span.context().traceId())) {
			return null;
		}
		return span.context().traceId();
	}
}
