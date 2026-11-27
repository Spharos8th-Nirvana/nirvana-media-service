package com.nirvana.media.global.response;

// 실패 응답의 error 객체. traceId는 구할 수 없으면 null로라도 항상 내보낸다. (MED-NFR-05)
public record ErrorResponse(
	String code,
	String message,
	String traceId
) {
}
