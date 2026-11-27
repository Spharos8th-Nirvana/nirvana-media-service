package com.nirvana.media.global.auth;

import java.util.UUID;

// Gateway가 넘겨 준 인증 헤더로 만든 요청자 정보. 업로드한 사람(createdBy)에는 요청 본문의 값 대신 이것만 쓴다. (MED-UPL-05)
public record Requester(
	UUID accountUuid,
	AccountType accountType
) {
}
