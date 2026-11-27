package com.nirvana.media.global.auth;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import com.nirvana.media.global.exception.BusinessException;
import com.nirvana.media.global.exception.ErrorCode;

// Gateway가 JWT를 검증한 뒤 넣어 준 인증 헤더 값으로 Requester를 만든다. (AUTH-36)
// 헤더를 읽는 곳이 여러 군데가 되어도 같은 규칙을 쓰도록 여기 한 곳에 둔다.
final class RequesterHeaders {

	static final String USER_ID = "X-User-Id";
	static final String ACCOUNT_TYPE = "X-Account-Type";

	// UUID.fromString은 "1-1-1-1-1"처럼 짧은 값도 받아 주므로 36자 표준 형식인지 먼저 확인한다.
	private static final Pattern UUID_PATTERN =
		Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

	private RequesterHeaders() {
	}

	// 값이 없거나 형식이 틀리면 UNAUTHORIZED(401).
	static Requester parse(@Nullable String userId, @Nullable String accountType) {
		return new Requester(parseAccountUuid(userId), parseAccountType(accountType));
	}

	private static UUID parseAccountUuid(@Nullable String userId) {
		if (userId == null || !UUID_PATTERN.matcher(userId).matches()) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
		return UUID.fromString(userId);
	}

	private static AccountType parseAccountType(@Nullable String accountType) {
		return Arrays.stream(AccountType.values())
			.filter(type -> type.name().equals(accountType))
			.findFirst()
			.orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
	}
}
