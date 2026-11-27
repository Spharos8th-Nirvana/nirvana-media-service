package com.nirvana.media.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

// 모든 일반 JSON 응답의 공통 형식. 화면이 항상 같은 세 필드로 분기할 수 있도록 null 필드도 내보낸다. (MED-NFR-05)
// 성공: success=true, error=null / 실패: success=false, data=null
@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(
	boolean success,
	T data,
	ErrorResponse error
) {

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null);
	}

	public static ApiResponse<Void> failure(ErrorResponse error) {
		return new ApiResponse<>(false, null, error);
	}
}
