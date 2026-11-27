package com.nirvana.media.global.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 플랫폼 관리자(ADMIN)만 부를 수 있는 컨트롤러나 메서드에 붙인다. 예: 관리자 강제 삭제(MED-DEL-05)
// AdminOnlyInterceptor가 Service 호출 전에 확인한다.
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminOnly {
}
