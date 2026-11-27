package com.nirvana.media.global.auth;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.nirvana.media.global.exception.BusinessException;
import com.nirvana.media.global.exception.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// @AdminOnly가 붙은 요청은 인증 헤더가 없거나 틀리면 401, ADMIN이 아니면 403으로 막는다.
// 던진 예외는 GlobalExceptionHandler가 공통 오류 형식으로 바꾼다.
public class AdminOnlyInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (!(handler instanceof HandlerMethod handlerMethod) || !isAdminOnly(handlerMethod)) {
			return true;
		}
		Requester requester = RequesterHeaders.parse(
			request.getHeader(RequesterHeaders.USER_ID), request.getHeader(RequesterHeaders.ACCOUNT_TYPE));
		if (requester.accountType() != AccountType.ADMIN) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
		return true;
	}

	private boolean isAdminOnly(HandlerMethod handlerMethod) {
		return handlerMethod.hasMethodAnnotation(AdminOnly.class)
			|| handlerMethod.getBeanType().isAnnotationPresent(AdminOnly.class);
	}
}
