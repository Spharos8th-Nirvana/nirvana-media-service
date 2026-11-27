package com.nirvana.media.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// createdAt, updatedAt을 자동으로 채운다.
// 메인 애플리케이션 클래스가 아니라 따로 두어 @WebMvcTest 같은 슬라이스 테스트가 JPA 없이 뜨게 한다.
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
