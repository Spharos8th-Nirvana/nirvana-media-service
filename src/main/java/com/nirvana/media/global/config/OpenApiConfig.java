package com.nirvana.media.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

// Swagger 화면 설정. 화면의 Authorize 버튼에서 Gateway 인증 헤더 2개를 넣고 API를 호출할 수 있게 한다.
@Configuration
public class OpenApiConfig {

	// global.auth.RequesterHeaders의 헤더 이름과 같아야 한다. 그 상수는 패키지 전용이라 여기 따로 둔다.
	private static final String USER_ID_HEADER = "X-User-Id";
	private static final String ACCOUNT_TYPE_HEADER = "X-Account-Type";

	@Bean
	public OpenAPI mediaOpenApi() {
		return new OpenAPI()
			.info(new Info()
				.title("미디어 서비스 API")
				.description("업로드 URL 발급, 업로드 완료 확인, 파일 정보 조회, 연결 가능 여부 확인 API. "
					+ "인증 헤더는 Gateway가 JWT를 검증한 뒤 넣어 준다. 로컬에서는 Authorize 버튼으로 직접 넣는다.")
				.version("v1"))
			.components(new Components()
				.addSecuritySchemes(USER_ID_HEADER, headerScheme(USER_ID_HEADER, "계정 UUID (36자 표준 형식)"))
				.addSecuritySchemes(ACCOUNT_TYPE_HEADER, headerScheme(ACCOUNT_TYPE_HEADER, "USER, OPERATOR, ADMIN 중 하나")))
			// 두 헤더를 함께 보내야 하므로 요구사항 하나에 둘 다 넣는다.
			.addSecurityItem(new SecurityRequirement().addList(USER_ID_HEADER).addList(ACCOUNT_TYPE_HEADER));
	}

	private static SecurityScheme headerScheme(String headerName, String description) {
		return new SecurityScheme()
			.type(SecurityScheme.Type.APIKEY)
			.in(SecurityScheme.In.HEADER)
			.name(headerName)
			.description(description);
	}
}
