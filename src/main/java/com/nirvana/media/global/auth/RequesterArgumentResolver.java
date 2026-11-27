package com.nirvana.media.global.auth;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

// @CurrentRequester Requester 파라미터에 Gateway 인증 헤더로 만든 요청자 정보를 넣는다. (MED-UPL-05, AUTH-36)
public class RequesterArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CurrentRequester.class)
			&& Requester.class.equals(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		return RequesterHeaders.parse(
			webRequest.getHeader(RequesterHeaders.USER_ID), webRequest.getHeader(RequesterHeaders.ACCOUNT_TYPE));
	}
}
