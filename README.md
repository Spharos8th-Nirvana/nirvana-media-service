# nirvana-media-service

## 서비스 개요

- 목적: 미디어 서비스. 파일 자체만 관리합니다. 업로드 URL 발급과 완료 확인, 메타데이터 조회, 연결 가능 여부 확인, 붙음·빠짐 상태 변경, 미사용 파일 정리를 맡습니다.
- 담당자 / 백업 리뷰어: @xoxxi / <!-- 확정 후 기입 -->
- 소유 데이터와 운영 책임: `nirvana_media` 스키마. 파일이 어느 글·사찰·프로그램에 어떤 순서로 붙었는지는 각 도메인 서비스의 연결 테이블(`postMedia`, `templeMedia`, `programMedia` 등)이 관리합니다(UUID 참조, FK 없음).
- API 문서: [Swagger UI](http://localhost:8085/swagger-ui/index.html) (local 프로필)
- 기본 포트 / 헬스체크: `8085` / `GET /actuator/health`

| 항목 | 값 |
| --- | --- |
| Gradle 프로젝트 | `nirvana-media-service` |
| Java 패키지 | `com.nirvana.media` |
| 시작 클래스 | `MediaServiceApplication` |
| `spring.application.name` | `media-service` → Config Repo `media-service*.yml` |

## 로컬 실행

- 사전 요구사항은 JDK 21입니다.
- macOS와 Linux: `./gradlew bootRun`
- Windows PowerShell: `.\gradlew.bat bootRun`
- Windows Git Bash: `./gradlew bootRun`
- 기본 local 프로필은 Config Server 없이 시작하며 Swagger UI를 활성화합니다.
- 다른 프로필에서는 API 문서와 Swagger UI가 기본으로 비활성화됩니다.

### 로컬 DB (MySQL)

local 프로필은 `localhost:13309`의 `nirvana_media` 스키마에 접속합니다. 아래 값은 로컬 전용 기본값입니다.

1. MySQL 컨테이너를 실행합니다.

   ```bash
   docker run -d --name media-mysql -p 13309:3306 \
     -e MYSQL_ROOT_PASSWORD=root \
     -e MYSQL_DATABASE=nirvana_media \
     -e MYSQL_USER=media -e MYSQL_PASSWORD=media \
     mysql:8.0 --character-set-server=utf8mb4 --collation-server=utf8mb4_0900_ai_ci
   ```

2. 스키마 DDL(`nirvana-app/db/schema/03_media.sql`)을 적용합니다. `<schema-dir>`은 DDL 폴더 경로입니다.

   ```bash
   docker exec -i media-mysql mysql -umedia -pmedia nirvana_media < <schema-dir>/03_media.sql
   ```

3. `./gradlew bootRun` 후 `GET /actuator/health`의 `db`가 `UP`인지 확인합니다.

- 접속 정보는 환경변수 `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`로 바꿀 수 있습니다.
- JPA `ddl-auto`는 `validate`입니다. 테이블은 DDL로 만들고 애플리케이션이 생성하지 않습니다.
- DDL 컬럼이 camelCase라 Entity 필드 이름이 그대로 컬럼 이름이 됩니다(`PhysicalNamingStrategyStandardImpl`).

### Config Server 사용

- `SPRING_PROFILES_ACTIVE=config-server`로 설정합니다.
- 로컬 Swagger UI와 로컬 DB도 함께 사용하려면 `local,config-server`로 설정합니다.
- `CONFIG_SERVER_URL` 기본값은 `http://localhost:8888`입니다.
- config-server 프로필에서는 Config Server가 떠 있어야 시작합니다. 기본 local 프로필은 Config Server 없이 시작합니다.

## 빌드·테스트

macOS, Linux, Windows Git Bash:

```bash
./gradlew build
```

Windows PowerShell:

```powershell
.\gradlew.bat build
```

JAR만 빌드하려면 bootJar를 사용합니다.
현재 기본 CI는 develop·main 대상 PR에서 bootJar를 실행합니다.
build에 포함된 테스트 실행과 CI의 실제 검사 범위는 구분해서 확인하세요.

## 패키지 구조

```
com.nirvana.media
├── global/   # 여러 도메인이 같이 쓰는 코드: 공통 Entity 기반, 설정
└── domain/   # 도메인별 코드
    └── media/
```

새 코드는 그 코드가 다루는 테이블을 맡은 도메인 패키지에 둡니다.
여러 도메인이 같이 쓰는 코드만 `global`에 둡니다.

| 패키지 | 맡는 테이블 | 요구사항 |
| --- | --- | --- |
| `media` | media | MED-UPL, MED-GET, MED-ATT, MED-DEL, MED-INT, MED-NFR |

`package-info.java`에 맡는 일과 요구사항 ID 범위가 적혀 있습니다.

### 도메인 패키지 안의 계층

도메인 패키지 안은 계층별 하위 패키지로 나눕니다. 예: `domain/media/service/MediaService.java`

| 하위 패키지 | 두는 것 |
| --- | --- |
| `controller` | HTTP 요청·응답 처리, 요청 DTO 검증 |
| `service` | 비즈니스 로직, 권한 검증, 트랜잭션 |
| `repository` | Entity 조회·저장 (DB 접근) |
| `entity` | Entity와 도메인 상태 규칙, 상태 enum |
| `dto` | API 요청·응답 객체 |

- 호출 방향은 `controller → service → repository`입니다. Controller가 Repository를 직접 부르지 않습니다.
- 하위 패키지는 미리 만들지 않습니다. 그 계층의 첫 클래스를 만들 때 생깁니다.
- `outboxEvents`, `processedEvents`는 도메인이 아니라 Kafka 연동용 테이블이라, Kafka 작업 때 `global` 아래에 둡니다.

### 공통 Entity 기반 (`global/entity`)

| 클래스 | 컬럼 | 채우는 방법 |
| --- | --- | --- |
| `BaseTimeEntity` | `createdAt`, `updatedAt` | JPA Auditing이 저장·수정할 때 자동으로 채웁니다(`JpaAuditingConfig`). |
| `BaseAuditEntity` | 위 두 컬럼 + `createdBy`, `updatedBy` | Entity가 `recordCreator(uuid)`, `recordModifier(uuid)`로 직접 넘깁니다. |

- `createdBy`, `updatedBy`에는 요청 본문의 값이 아니라 인증된 요청자나 이벤트 행위자의 UUID를 넘깁니다(MED-UPL-05).
- `updatedBy`는 한 번도 바뀌지 않은 행에서 `NULL`입니다.

## 개발 규칙

개발과 기여 방법은 [CONTRIBUTING.md](CONTRIBUTING.md)를 참고하세요.
