package com.nirvana.media.global.entity;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

// 시각에 더해 만든 사람(createdBy)과 마지막으로 바꾼 사람(updatedBy)이 있는 테이블용 기반 클래스. 예: media
// 처리자는 HTTP 요청자만이 아니라 이벤트의 행위자일 수도 있어서, 자동으로 채우지 않고 Entity가 직접 넘긴다.
// 요청 본문의 사용자 값이 아니라 인증된 요청자 UUID를 넘긴다. (MED-UPL-05)
@Getter
@MappedSuperclass
public abstract class BaseAuditEntity extends BaseTimeEntity {

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(nullable = false, updatable = false, length = 36)
	private UUID createdBy;

	// 한 번도 바뀌지 않은 행은 NULL이다.
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(length = 36)
	private UUID updatedBy;

	protected void recordCreator(UUID createdBy) {
		this.createdBy = createdBy;
	}

	protected void recordModifier(UUID updatedBy) {
		this.updatedBy = updatedBy;
	}
}
