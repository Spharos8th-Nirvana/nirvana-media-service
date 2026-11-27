/**
 * 미디어 도메인. 파일 자체만 맡는다: 업로드 URL 발급과 완료 확인, 메타데이터 조회, 연결 가능 여부 확인,
 * 붙음·빠짐 상태 변경, 미사용·삭제 표시 파일 정리.
 * 어디에 어떤 순서로 붙었는지, 대표 사진이 무엇인지는 각 도메인의 연결 테이블이 맡는다.
 *
 * <p>테이블: media
 * <p>요구사항: MED-UPL-01 ~ MED-UPL-06, MED-GET-01 ~ MED-GET-05, MED-ATT-01 ~ MED-ATT-03,
 * MED-DEL-01 ~ MED-DEL-05, MED-INT-01 ~ MED-INT-05, MED-NFR-01 ~ MED-NFR-06
 */
package com.nirvana.media.domain.media;
