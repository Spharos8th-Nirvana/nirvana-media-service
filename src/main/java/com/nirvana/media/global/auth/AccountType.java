package com.nirvana.media.global.auth;

// 요청자 역할. Gateway의 X-Account-Type 값과 같다. (AUTH-33)
public enum AccountType {

	USER,
	OPERATOR,
	ADMIN
}
