package com.nirvana.media.global.exception;

import lombok.Getter;

// 예상 가능한 업무 오류. GlobalExceptionHandler가 ErrorCode의 상태코드와 공통 오류 형식으로 바꾼다.
@Getter
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
