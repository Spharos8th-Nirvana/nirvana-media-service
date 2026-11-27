package com.nirvana.media.global.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 컨트롤러의 Requester 파라미터에 붙인다. 인증 헤더가 없거나 틀리면 401(UNAUTHORIZED)로 막는다.
// 미디어 API는 비회원이 부르지 않으므로 헤더 없이 통과하는 선택 옵션은 두지 않는다.
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentRequester {
}
