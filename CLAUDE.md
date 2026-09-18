# PhotoLog 프로젝트 인수인계 문서

KDT 풀스택 부트캠프 수료 직후 시작한 1인 포트폴리오 프로젝트. 이 문서는 Cowork(Claude)와 나눈 기획·구현 대화를 정리한 것으로, Claude Code로 넘어가서 이어서 개발할 때 컨텍스트로 쓰기 위한 것입니다.

## 1. 프로젝트 개요

- **이름**: PhotoLog
- **컨셉**: 출사지 공유 + EXIF 기반 사진·장비 분석 사진 커뮤니티, AI 기능 포함
- **목표**: (1) CRUD·인증·페이징 같은 기본기를 확실히 다지기, (2) EXIF·기상·지도·AI API를 엮은 특화 기능으로 포트폴리오 차별화
- **개발 원칙**: 코드는 본인이 한 줄씩 직접 작성한다. AI(Claude)는 코드를 대신 짜주지 않고 리뷰·개념 설명·막힌 부분 조언만 한다. **유일한 예외**: Spring Security + JWT는 이미 짜여진 스켈레톤 코드가 있어서, 그걸 함께 한 줄씩 읽으며 원리를 이해하는 데 집중한다 (이 경우도 AI가 새로 코드를 작성해주진 않음).
- **저장소**: https://github.com/sai0734/PhotoLog (Public, 모노레포 — `frontend/`, `backend/` 같이 포함)
- **로컬 경로**: `C:\Users\hjc13\sai\PhotoLog` — 원래 `OneDrive\바탕 화면\PhotoLog`에 있었으나, **한글 경로 때문에 Gradle 테스트 실행 시 ClassNotFoundException이 발생**해서 영문 경로로 이동함. 앞으로도 한글·OneDrive 경로는 피할 것.

## 2. 기술 스택

| 영역 | 스택 |
|---|---|
| Frontend | React(Vite), TypeScript(**미착수** — `tsconfig.json` 없음, `.ts/.tsx` 0개, 본인이 직접 설정 예정), Redux Toolkit, Axios, **Tailwind CSS**(`tailwindcss ^3.4.19` — 스켈레톤에서 넘어옴, 유지/Vanilla CSS 교체 미결정), `exifr`(EXIF 파싱), SunCalc(월령 계산) |
| Backend | JDK 21, Spring Boot 3.x, Spring Security + JWT(jjwt), Spring Data JPA(Hibernate), MariaDB |
| AI | Python 3.10+, FastAPI, LangChain, ChromaDB(RAG), Ollama(로컬 LLM, 1순위) 또는 Groq(무료 API, 대안) |
| 외부 API | Kakao Map API, OpenWeatherMap(무료 티어 5일 예보 한도), TossPayments(결제 데모, 스트레치) |
| Cache | Redis(**도입 확정, 착수 전** — Lettuce 클라이언트, `spring-boot-starter-data-redis`) |
| 툴 | Postman, Swagger UI |

**AI 엔진 관련 의사결정**: 원래 Unsloth 파인튜닝 + vLLM 서빙을 고려했으나, GPU 확보·데이터셋 구축·학습 사이클 등 1인 개발 일정에 리스크가 커서 Ollama/Groq + RAG 조합으로 변경. "독립 AI 백엔드 구축, RAG 파이프라인 설계, 도메인 특화 프롬프트 엔지니어링" 스토리는 유지하면서 인프라 리스크만 제거.

**Redis/Kafka 관련 의사결정 (2026-09-17)**: **Redis 도입 확정.** 로컬에 Redis 서버 설치 + `spring-boot-starter-data-redis`(내부적으로 Lettuce 클라이언트 드라이버 사용, MariaDB의 `mariadb-java-client`와 같은 역할) 추가 예정. REST API처럼 HTTP로 통신하는 게 아니라, RESP 프로토콜 기반 TCP 커넥션 풀을 통해 `RedisTemplate.opsForValue()` 같은 메서드 호출로 사용 — 통신 방식은 REST API보다 오히려 JDBC(MariaDB 연결)에 가까움. 용도 후보: JWT Refresh Token 블랙리스트(로그아웃 시 실제 토큰 무효화), OpenWeatherMap 응답 캐싱(④ 야간 출사지도, 무료 티어 호출 횟수 절약). 정확한 도입 시점·구현 범위는 미정. **Kafka는 도입 여부 보류(미정)** — 붙인다면 ⑤ AI 서비스 단계에서 "사진 업로드 → AI 피드백 생성" 흐름을 비동기 이벤트로 처리하는 스트레치 골로 고려 중이나, 1인 로컬 데모 규모상 실질적 필요성보다는 학습·포트폴리오 목적에 가까움. **우선순위는 변하지 않음 — 게시판(②) 구현이 여전히 다음 작업.**

**Persistence 관련 의사결정 (2026-09-15)**: 회원 기능을 MyBatis(Mapper 인터페이스 + XML SQL)로 구현 완료한 뒤, 프로젝트 전체를 Spring Data JPA(Hibernate)로 전환하기로 결정. 참고용으로 보유하고 있던 KDT 부트캠프 JPA 버전 스켈레톤(`back_JPA.zip`)의 `domain`/`repository`/`service` 패턴을 그대로 따름. 스키마 관리도 수동 `schema.sql`(`spring.sql.init.mode=always`)에서 `spring.jpa.hibernate.ddl-auto=update`로 함께 전환, `schema.sql` 파일은 삭제. `Member`/`MemberRole` 기준으로 전환·빌드 후, `MemberRepositoryTests`로 계정을 생성해 프론트엔드 로그인까지 검증 완료(회원가입 화면 자체는 14절 기록대로 아직 미구현이라 테스트 코드로 계정을 만듦). **이후 게시판부터는 처음부터 JPA로 구현**하며, MyBatis 관련 설명은 이 문서에서 전부 JPA 기준으로 갱신함.

## 3. 시스템 아키텍처

배포 없이 로컬 5개 서비스를 개별 수동 기동 (Docker는 스트레치 골):

| 서비스 | 포트 | 역할 |
|---|---|---|
| React (Vite) | 3000 | 클라이언트 UI, `exifr`로 EXIF 즉시 파싱, Spring Boot에만 요청 |
| Spring Boot | 8080 | REST API, 인증, 일반 CRUD → MariaDB 직접 통신 |
| MariaDB | 3306 | 게시글·EXIF·GPS·회원·결제 데이터 |
| FastAPI | 8000 | AI 백엔드 — RAG 파이프라인, 프롬프트 조립 |
| Ollama | 11434 | 로컬 오픈소스 LLM 서빙 (대안: Groq) |

흐름: `React → Spring Boot → MariaDB` (기본), AI 요청일 때만 `Spring Boot → FastAPI → Ollama/Groq`.

EXIF는 서버 왕복 없이 **프론트엔드에서 `exifr`로 즉시 파싱**한다. 업로드 순간 카메라·렌즈·F값·셔터스피드·ISO·GPS를 화면에 채워 보여주고, 사용자가 확인/수정(필름 카메라는 수동 입력) 후 "게시" 시점에 확정된 메타데이터를 이미지와 함께 Spring Boot로 전송. Spring은 파싱 없이 저장만 담당하고 값 범위만 최소 검증.

## 4. 공통 설계 원칙

**상태관리 전략**

| 기능 | 방식 | 비고 |
|---|---|---|
| 인증 | RTK | `userSlice` — 앱 전역 로그인 상태 |
| 게시판 | Props | 컴포넌트 트리가 얕아 prop drilling으로 충분 |
| 사진 갤러리 | RTK + Custom Hook | 모달·무한스크롤·지도가 상태 공유 — `useGallery()` 등으로 캡슐화 |
| 야간 출사지도 | RTK | 날짜·지역·날씨 응답을 여러 컴포넌트가 동시 참조 |
| AI 서비스 | RTK | 입력값·응답/로딩 상태를 여러 UI가 동시 참조 |

**파일 저장 정책**: 업로드 이미지(프로필, 게시글 첨부, 갤러리 사진)는 클라우드 스토리지 없이 Spring 프로젝트의 `upload` 폴더에 저장, DB엔 경로만 저장. `.gitignore`로 제외하되, `CustomFileUtil`의 `@PostConstruct` 초기화 로직이 앱 구동 시 폴더가 없으면 자동 생성하므로 `.gitkeep`은 불필요.

**패키지 구조 원칙(백엔드)**: 기능 단위 패키지 구조(package-by-feature). 아래 5절 참고. 게시판·갤러리 등 새 기능도 동일 컨벤션(`com.backend.board`, `com.backend.gallery`)을 따를 것.

**개발 순서 원칙**: 5개 핵심 기능을 동시에 벌리지 않고 순서대로 하나씩. 각 기능은 화면-API-DB가 끝까지 연결되는 최소 동작 버전(뼈대)을 먼저 완성해 파이프라인이 도는 걸 확인한 뒤, 예외 처리·유효성 검증·UX 디테일을 단계적으로 채운다.

**API 검증 절차 (2026-09-17 확정, 새 API 만들 때마다 매번 적용)**: 새 Repository/Service/Controller를 추가할 때마다 아래 순서로 검증한다. 본인이 잊기 쉬워서 Claude가 매번 먼저 상기시켜주기로 함.
1. **Repository 테스트** — 새/변경된 `JpaRepository` 메서드를 검증하는 테스트 작성 (`MemberRepositoryTests` 참고)
2. **Service 테스트** — Service 레이어에 로직이 있으면 그것도 테스트
3. **Postman으로 Controller 엔드포인트 직접 호출** — 상태 코드·응답 바디·인증 헤더 동작 확인. 본인이 Postman 사용에 아직 익숙하지 않아서 **연습 목적으로 매번 진행** (요청 메서드/URL/헤더/바디 설정을 매번 구체적으로 안내받기로 함)
4. **브라우저 개발자도구 Network 탭 확인** — 프론트 연동까지 끝난 뒤, 실제 요청/응답 페이로드를 열어서 프론트가 보내는 값과 백엔드가 기대하는 값이 맞는지 확인

## 5. 현재 백엔드 패키지 구조 (실제 구현됨, 검증 완료)

```
com.backend
├── BackendApplication.java   (JPA는 @MapperScan 등 별도 스캔 설정 불필요)
├── board/                     ← 게시판 기능 전용 (2026-09-18 착수)
│   ├── domain/                   Board(@Entity, @Table(name="tbl_board"), BaseEntity 상속 — regDate/modDate 자동 기록 정상 동작 확인(2026-09-18, 아래 BaseEntity 참고)),
│   │                              BoardImage(@Entity, @Table(name="tbl_board_image"), Board와 @ManyToOne/@OneToMany(mappedBy="board"))
│   ├── dto/                      BoardDTO
│   ├── repository/               BoardRepository — findKeyword(검색), findWithMember/findWithAllMember(@EntityGraph로 memberEmail 즉시 로딩, findById/findAll 대체용 — 8절 6번 참고)
│   └── service/                  BoardService, BoardServiceImpl (insert/getBoard/getBoardList/modify/delete 5개 CRUD, Repository+Service 테스트 완료)
├── global/                    ← 여러 기능이 공유하는 것
│   ├── config/                   CustomSecurityConfig, CustomServletConfig, RootConfig
│   ├── controller/advice/        CustomControllerAdvice
│   ├── controller/formatter/     LocalDateFormatter
│   ├── domain/                    BaseEntity (regDate/modDate — 2026-09-18 완료: `@MappedSuperclass`+`@EntityListeners(AuditingEntityListener)`+`BackendApplication`의 `@EnableJpaAuditing`+`BaseEntity` 자체에 `@Getter` 4가지 다 적용, DB 직접 조회로 `regdate`/`moddate` 실제 타임스탬프 기록 확인 완료)
│   ├── dto/                      PageRequestDTO, PageResponseDTO
│   └── util/                     CustomFileUtil
├── member/                    ← 회원 기능 전용
│   ├── controller/                MemberController
│   ├── domain/                    Member(@Entity, @Table(name="tbl_member")), MemberRole
│   ├── dto/                       MemberDTO, MemberModifyDTO
│   ├── repository/                MemberRepository (JpaRepository<Member, String> 상속)
│   └── service/                   MemberService, MemberServiceImpl
└── security/                  ← 인증/인가 인프라
    ├── controller/                APIRefreshController
    ├── service/                   CustomUserDetailsService
    ├── filter/                    JWTCheckFilter
    ├── handler/                   APILoginFailHandler, APILoginSuccessHandler, CustomAccessDeniedHandler
    └── securityutil/              CustomJWTException, JWTUtil
```

새 Repository 인터페이스는 `JpaRepository<Entity, PK타입>`을 상속하기만 하면 Spring Data JPA가 런타임에 구현체를 자동 등록한다 (MyBatis처럼 별도 애노테이션·스캔 설정 불필요). 새 엔티티를 추가할 때 주의할 점:
- PK가 자동증가면 `@GeneratedValue(strategy = GenerationType.IDENTITY)`를 붙일 것 (이메일처럼 자연키면 붙이지 않음 — `Member.email` 참고)
- 테이블명을 명시하려면 `@Table(name="tbl_xxx")`를 반드시 붙일 것 — 안 붙이면 Hibernate가 클래스명 기준 기본 네이밍 전략으로 다른 이름의 테이블을 만들어버림
- 지연 로딩(`fetch = FetchType.LAZY`)으로 연관된 컬렉션을 트랜잭션 밖에서 접근하면 `LazyInitializationException`이 날 수 있으므로, 필요하면 `@EntityGraph` + `@Query`로 즉시 로딩하는 조회 메서드를 따로 만든다 (`MemberRepository.getWithRoles(email)` 참고)

## 6. 핵심 기능 명세 (구현 순서: 회원 → 게시판 → 갤러리 → 야간출사지도 → AI서비스)

### ① 회원 및 인증 (MVP) — ⚠️ 부분 구현 (로그인 + 정보수정만 동작, JPA 전환 완료 — 14절 참고)
- 목표 페이지: 로그인 / 회원가입(아이디·비밀번호·닉네임 + 아이디 중복확인 + 프로필사진 1장 멀티파트) / 마이페이지(닉네임·프로필사진 수정, 회원탈퇴)
- **실제 구현 현황**: 로그인(Spring formLogin `/api/member/login`) + `PUT /api/member/modify` + `GET /api/member/refresh`만 존재 (Persistence는 MyBatis → Spring Data JPA로 전환·재검증 완료, 2절 Persistence 관련 의사결정 참고). 회원가입·아이디 중복확인·프로필사진 업로드·회원탈퇴(소프트 삭제)는 **엔드포인트/스키마/프론트 모두 미구현**
- JWT 기반 인증(BCrypt 암호화, Access/Refresh Token) — 기존 스켈레톤 코드 리딩으로 이해
- 회원탈퇴는 **소프트 삭제** 예정: 탈퇴 후에도 게시글·사진은 유지, 작성자 표시만 "탈퇴한 회원" 등으로 대체
- 스트레치: 이메일 인증, 소셜 로그인, 관리자 페이지 회원관리
- 로그인/로그아웃/정보수정 흐름 동작 확인 완료, 이후 MyBatis → Spring Data JPA로 전환하고 재검증 완료 (단, `ModifyComponent` 비번 덮어쓰기 버그 있음 — 14절)

### ② 자유게시판 (MVP) — 🔧 백엔드 CRUD Controller 완료, 이미지·댓글·프론트·Postman 검증 남음 (2026-09-18)
- **`BoardController` CRUD 5개 완료**: `GET /api/board`(목록), `GET /api/board/{boardNumber}`(단건), `POST /api/board`(등록), `PUT /api/board/{boardNumber}`(수정), `DELETE /api/board/{boardNumber}`(삭제) — 컴파일·`contextLoads()` 통과 확인. 등록/수정/삭제는 `Principal.getName()`으로 작성자 이메일을 서버가 직접 채움(클라이언트가 못 정함), 전부 `@PreAuthorize("hasAnyRole('USER')")`로 로그인 필요. `insert()`는 `MemberRepository.findById(email)`로 실제 조회해서 `Board.builder()`로 직접 조립(`ModelMapper` 안 씀). `modify`/`delete`는 `findWithMember()`(즉시로딩)로 통일, 소유권 체크(`AccessDeniedException`) 포함
- **남은 것 (2026-09-18 기준, 순서 무관하게 전부 미완료)**:
  1. **다중 이미지 업로드/조회/삭제** — 오늘 밤 집 PC에서 이어서 할 것: `BoardImageRepository` 신규 → `BoardService.addImages`/`removeImage` 추가(`CustomFileUtil` 활용) → `BoardController`에 `POST /{boardNumber}/images`, `DELETE /{boardNumber}/images/{boardImageNumber}`, `GET /files/{fileName}`(파일 조회용, 빠뜨리기 쉬움) 3개 엔드포인트 추가
  2. **게시글 전체 삭제 시 실제 이미지 파일 정리** — 지금 `delete()`는 JPA cascade로 `BoardImage` DB 행만 지움, 디스크의 실제 파일은 안 지워짐. 이미지 기능 만든 뒤 `delete()`에 `customFileUtil.deleteFiles(...)` 호출 추가 필요
  3. **댓글·대댓글(2단계 제한)** — 완전히 미착수, `Comment` 엔티티부터 새로 설계해야 함
  4. **프론트엔드(.jsx)** — 게시판 화면 자체가 하나도 없음 (리스트/상세/등록/수정 페이지 전부 미착수)
  5. **Postman 실제 검증** — 컴파일·컨텍스트 로딩 확인만 했고, 실제 HTTP 요청으로 5개 엔드포인트를 호출해본 적은 아직 없음 (4절 API 검증 절차 3번)
  6. (선택) `BoardRepositoryTest`/`BoardServiceImplTest`에 `@Transactional` 추가 — 하드코딩 ID가 테스트 반복 실행마다 어긋나는 문제, 당장 급하지 않아 보류 중
- 페이지: 리스트(페이징) / 상세 / 등록(다중 이미지 업로드) / 수정(삭제 기능 포함)
- **다중 이미지 처리 방식 (2026-09-18 결정)**: 참고한 `baby_project`(`CommunityPost`)처럼, 게시글 본문 수정(`PUT /api/board/{id}`)과 이미지 추가/삭제를 **별도 엔드포인트로 분리**하기로 결정. `modify()`는 `title`/`contents`만 다루고 이미지는 안 건드림. `BoardDTO.imageUrl`(`List<String>`)과 `Board.imageUrl`(`List<BoardImage>`) 타입이 달라 `ModelMapper`가 자동 변환 못 하므로, 이미지 추가 로직은 `stream().map()`으로 직접 `BoardImage` 리스트를 만들어야 함(`baby_project`의 `CommunityPostServiceImpl.addImages()`/`toDTO()` 참고). 이미지 삭제 식별자는 `baby_project`(fileName 문자열)와 다르게, `BoardImage`가 이미 자체 PK(`boardImageNumber`)가 있으므로 **그걸로 식별하기로 결정**
- 스트레치: 카테고리 필터, 드래그앤드롭·클립보드 붙여넣기 업로드

### ③ 사진 갤러리 (MVP) — 미착수
- 페이지: 무한스크롤 그리드 / 모달(`exifr`로 프론트에서 파싱한 EXIF 패널 + Kakao Map 좌표 레이어) / 등록(다중 업로드 + 필름카메라용 수동 입력 UI) / 수정·삭제
- EXIF는 업로드 즉시 프론트에서 추출·미리보기, 사용자 확인/수정 후 게시 시점에 서버 전송·저장
- 스트레치: TossPayments 결제 연동 데모 — 모달에 "구매하기" 버튼 → 결제위젯(테스트 카드) → 서버 승인 API → 성공/실패 페이지, 마이페이지에 결제 목록 페이지. **실제 마켓플레이스가 아니라 결제 연동 자체를 보여주는 데모** 목적이라 판매자 정산·실물 배송 로직은 다루지 않음

### ④ 야간 출사지도 (MVP) — 미착수
- 날짜 선택: 오늘부터 5일 이내 (OpenWeatherMap 무료 티어 예보 제공 범위)
- 대표 지역 9곳(서울, 강원 평창, 경기 가평, 충청 태안, 전북 무주, 전남 고흥 나로도, 경북 영양, 경남 거창, 제주) 좌표 코드에 고정, 이후 실제 유명 출사지로 보강
- 구름량·강수확률=OpenWeatherMap, 월령·월광도=SunCalc, 광해도=지역별 정적 근사 테이블 → Kakao Map 마커 표시
- 임의 지점 조회: 지도 클릭 시 모달로 동일 정보. 구름량·강수확률은 클릭 좌표로 바로 조회, 광해도는 최근접 대표 지역 값을 근사치로 사용
- 종합 점수: `score = 100 − (구름량×0.45 + 강수확률×0.20 + 월광도×0.20 + 광해도환산×0.15)` — 90+ 최적 / 70–89 좋음 / 50–69 보통 / 50미만 비추천
- 스트레치: 기상청 공공데이터 API 전환, 세분화된 실시간 광해도, 대표 지역 확장

### ⑤ AI 융합 서비스 (MVP) — 미착수
FastAPI + LangChain + ChromaDB(RAG) 위에서 Ollama(로컬) 또는 Groq(무료 API)로 서빙하는 세 기능:
- **AI 촬영 피드백**: EXIF(F값·셔터스피드·ISO·카메라) 분석해 전문가 시점 피드백 생성
- **맞춤형 출사 가이드**: ④의 날씨·월광도 데이터 + 사용자 장비 정보 조합해 노출 세팅 추천 — ④번과 실제로 데이터를 주고받는 지점
- **사진·장비 Q&A 챗봇**: 촬영 지식 문서를 ChromaDB에 임베딩, RAG 기반 자유 질문 답변
- 스트레치: 자체 파인튜닝(Unsloth QLoRA) + vLLM 서빙, 유사 사진 추천(임베딩 유사도 검색), 자동 캡션·태그 생성(비전 모델)

**우선순위 원칙**: 일정보다 완성도 우선. 촉박해지면 ⑤를 단순화(RAG 제거, 프롬프트만 사용) → ④의 임의 지점 조회·종합 점수 생략 순으로 자른다. ①②③은 기본기 평가의 핵심이라 끝까지 지킨다.

## 7. 시연/배포 & 로컬 개발환경 원칙

- 상시 클라우드 배포는 하지 않음 (비용·인프라 부담, 우선순위 아님). 대신 GitHub README(스크린샷, 아키텍처 다이어그램, 기술 의사결정 기록) + 데모 영상(GIF). 필요 시 로컬 라이브 시연.
- 5개 서비스는 처음엔 Docker 없이 손으로 개별 기동 — 각 서비스가 어떤 명령어로 뜨고 통신하는지 몸에 익히는 게 우선. Docker(볼륨 마운트+핫리로드 개발용 구성)는 기본기 익은 뒤 스트레치 골로 도입.
- 트러블슈팅 순서: 1차 자력 해결(에러 로그+공식 문서+구글링) → AI(Claude) 활용은 방향성 질문·코드 리뷰(보안, 쿼리 최적화, 타입 정의)·개념 설명 요청 위주.

## 8. 오늘 발견하고 고친 버그들 (재발 방지용 기록)

1. **`application.properties` DB URL 오타**: `.../photologdbuser`로 잘못 써서 실제 DB명(`photologdb`)이 아니었음. 수정 완료.
2. **`@MapperScan("com.backend.mapper")` — 옛 패키지 경로** *(MyBatis 시절 기록, 2026-09-15 JPA 전환으로 이 문제 자체가 해소됨 — `@MapperScan`/`@Mapper` 완전 제거)*: 폴더 구조를 `global/member/security`로 재편하면서 `MemberMapper`가 `com.backend.member.mapper`로 이동했는데 스캔 경로를 안 맞춰서 매퍼 빈이 등록 안 됨(`NoSuchBeanDefinitionException`). `@MapperScan(basePackages = "com.backend", annotationClass = Mapper.class)`로 수정하고, `MemberMapper`에 `@Mapper` 어노테이션 추가. **주의**: `annotationClass` 없이 그냥 `@MapperScan("com.backend")`만 하면, `com.backend` 밑의 모든 인터페이스(예: `MemberService`)를 매퍼로 오인식해서 가짜 빈이 생기고 실제 서비스 빈과 충돌할 수 있음 — 그래서 `annotationClass = Mapper.class`로 필터링함.
3. **MariaDB 계정 미생성**: `CREATE USER`/`CREATE DATABASE` SQL을 작성만 하고 실행을 안 해서, 앱 구동 시 존재하지 않는 계정으로 접속을 시도 → `GSS-API authentication exception` / `Unable to obtain Principal Name for authentication` 에러 발생. SQL을 실제로 실행해서 해결.
4. **한글 경로로 인한 Gradle 빌드/테스트 오류**: 프로젝트가 `OneDrive\바탕 화면\PhotoLog`(한글 경로 + OneDrive 동기화 폴더)에 있어서, `./gradlew clean test`를 해도 `ClassNotFoundException`이 반복 발생(컴파일은 성공하는데 테스트 워커 JVM이 클래스를 못 찾음). 콘솔에 한글 경로가 깨져서 출력되는 것도 방증. `C:\Users\hjc13\sai\PhotoLog`(영문 경로)로 프로젝트를 이동해서 해결. **앞으로 한글·OneDrive 경로는 피할 것.**
5. **JPA 전환 시 `Member` 테이블명 불일치**: `Member` 엔티티에 `@Table(name="tbl_member")`를 안 붙이면 Hibernate 기본 네이밍 전략상 `tbl_member`가 아니라 `member`라는 새 테이블을 찾음. `@Table(name="tbl_member")` 추가로 해결. 같은 이유로 `memberRoleList`(`@ElementCollection`)는 커스터마이징 안 하면 `member_member_role_list`라는 이름으로 자동 생성됨 — 참고한 JPA 스켈레톤(`back_JPA.zip`)도 동일하게 커스터마이징 없이 그대로 뒀으므로, PhotoLog도 동일하게 유지하기로 결정 (스키마를 `ddl-auto`로 완전히 넘겼으므로 옛 테이블명을 지킬 이유가 없음).
6. **`LazyInitializationException` — Entity를 DTO로 변환할 때 LAZY 연관관계를 트랜잭션 밖에서 건드리면 터짐 (2026-09-18, 게시판 작업 중 발견, 가장 중요한 교훈)**: `Board.memberEmail`이 `fetch = FetchType.LAZY`인 상태에서 `BoardServiceImpl.getBoard()`가 `boardRepository.findById(...)` → `modelMapper.map(board, BoardDTO.class)`로 DTO를 만들면, `memberEmail`은 아직 초기화 안 된 proxy 상태로 그대로 DTO에 복사됨. `@Transactional` 메서드가 끝나 세션이 닫힌 뒤(예: 테스트에서 `log.info(boardDTO)`, 나중엔 Controller가 JSON으로 직렬화할 때) 그 proxy를 실제로 읽으려 하면 `LazyInitializationException: ... no session`이 터짐. **해결**: `Member`처럼(`MemberRepository.getWithRoles()` 참고) `BoardRepository`에 `@EntityGraph(attributePaths = "memberEmail")` + `@Query`로 즉시 로딩 전용 조회 메서드(`findWithMember`/`findWithAllMember`)를 만들어서, `memberEmail`을 실제로 참조하는 조회(`getBoard`, `getBoardList`)에서만 `findById`/`findAll` 대신 이걸 씀. `memberEmail`을 안 건드리는 `modify`/`delete`는 그냥 `findById` 그대로 둬도 안전(관계를 안 쳐다보니까 proxy 초기화 자체가 안 일어남). **일반화된 규칙**: Entity를 조회해서 DTO로 변환해 트랜잭션 밖으로 내보낼 때, LAZY 연관관계 중 DTO에 실제로 담을 것만 선택적으로 즉시 로딩 처리한다 — 전부 EAGER로 바꾸는 건 성능상 안티패턴이라 하지 않음. 갤러리·댓글 등 앞으로 만들 모든 연관관계에 동일하게 적용될 원칙.

## 9. 코드 리뷰에서 발견했지만 의도적으로 그대로 둔 것들

- `APIRefreshController.checkTime((Integer)claims.get("exp"))` — **2026-09-15 실제 검증 완료**: 프로젝트가 쓰는 jjwt 0.11.5 + Jackson 조합으로 토큰 생성·파싱 테스트한 결과 `claims.get("exp")`는 `java.lang.Integer`로 들어와서 `(Integer)` 캐스팅 안전함(`ClassCastException` 안 터짐). 단, `exp`(Unix epoch 초)가 `Integer.MAX_VALUE`를 넘는 **2038년 이후엔 `Long`이 필요해져서 다시 터질 수 있음** — 그 전에 `(long) claims.get("exp")` 같은 안전한 방식으로 바꿔두는 게 정석이지만, 포트폴리오 프로젝트 수명 내에는 문제 없음.
- `MemberDTO.getClaims()`가 `pw`(BCrypt 해시)를 JWT claims에 포함시켜서, 발급된 access/refresh 토큰을 클라이언트가 디코딩하면 해시된 비밀번호가 노출됨. 로컬 전용 데모라 당장 위험은 낮다고 판단하고 그대로 둠. 다른 기능에서 claims 구조를 참고할 땐 `pw`는 빼는 게 정석.
- `RootConfig`의 `ModelMapper` 빈 — 현재는 아무도 안 쓰지만, 앞으로 게시판/갤러리에서 Entity↔DTO 변환에 쓸 계획이라 유지하기로 결정(지우지 않음).
- `pages/member/ModifyPage.jsx` 내부 변수명 오타(`ModfyPage`→`ModifyPage`) — 수정 완료.
- `CustomFileUtil.getFile()`의 `winter.jpg` 폴백 참조 — 실제로 존재하지 않는 파일이라 죽은 코드로 추정. 우선순위 낮아서 그대로 둠.

## 10. Git / GitHub

- 저장소: https://github.com/sai0734/PhotoLog (Public)
- 모노레포 구조: 최상위 `PhotoLog` 폴더 안에 `frontend/`, `backend/`를 함께 관리 (레포 하나)
- `git init`은 반드시 최상위 `PhotoLog` 폴더에서 (하위 폴더 안에서 하지 않음)
- 브랜치 전략: 혼자 하는 프로젝트라 브랜치를 따로 안 나누고 **`main`에서 직접 작업**하기로 결정
- 기본 워크플로우: `git add .` → `git commit -m "..."` → `git push` (`-u origin main`은 최초 1회만, 이후는 `git push`만)
- `backend/.gitignore`: `upload/`, `build/`, `.gradle/`, `.idea/`
- `frontend/.gitignore`: Vite 기본값 (`node_modules`, `dist` 등)
- **`application.properties`는 의도적으로 `.gitignore`에서 제외** — DB 계정정보(`photologdbuser`/`photologdbuser`, 로컬 데모용 단순 비밀번호)가 그대로 커밋되어 Public 저장소에 노출됨. 실서비스라면 절대 이렇게 하면 안 되지만, 로컬 전용 데모 프로젝트라는 걸 인지하고 감수하기로 한 결정.
- 다른 컴퓨터에서 이어서 개발하려면: `git clone` → (frontend) `npm install` → (backend) IntelliJ로 열거나 `./gradlew build` → 로컬에 MariaDB 계정/DB 새로 생성(`application.properties`에 있는 계정정보 그대로 맞춰서) → `upload/` 폴더는 앱이 자동 생성. (`CustomFileUtil.getFile()`이 이미지 없으면 404로 응답하도록 바뀌어서, 별도로 챙겨야 할 파일은 없음 — 14절 참고)

## 11. 네이밍 컨벤션

- 최상위 프로젝트 폴더 및 서브폴더(`frontend`, `backend`)는 소문자 시작
- Java 패키지: 전부 소문자. 클래스명: PascalCase
- JS/React: 컴포넌트 PascalCase, 훅/유틸 함수 camelCase
- MariaDB: DB명/유저명 대소문자 구분이 OS 의존적이라 소문자로 통일

## 12. 이전에 완료된 정리 작업 (참고용)

- KDT 스켈레톤 코드에 있던 Kakao 소셜로그인, 장바구니(cart), 상품(products), Todo 예제 모듈 전부 삭제
- 폴더/파일명 리네임: `photoLog_front`→`frontend`, `photoLog_back_Mybatis`→`backend`
- `SocialController.java`→`MemberController.java`로 리네임, Kakao 로직 제거하고 회원정보 수정 엔드포인트만 유지
- `API_SERVER_HOST` 상수를 `todoApi.js`에서 분리해 `api/host.js`로 이동 (`memberApi.js`, `util/jwtUtil.jsx`가 참조)
- `router/root.jsx`: todo/products 라우트 제거, member 라우트만 유지
- `components/menus/BasicMenu.jsx`: Todo/Products 메뉴 링크 제거
- (구) `schema.sql`: `tbl_member`, `tbl_member_role`만 유지 (cart/product/todo 테이블 제거) — 이후 JPA 전환으로 `schema.sql` 자체를 삭제하고 `ddl-auto`로 대체 (2절 참고)

## 13. 다음 할 일

**진행 순서 (2026-09-15 최종 확정)**

1. **14절 코드 리뷰 지적사항 정리** — ✅ 완료 (아래 내용은 2026-09-15에 전부 해결됨, 14절 참고). 남은 건 회원 기능(회원가입·중복확인·프로필사진·탈퇴) 자체와 CORS 참고 항목뿐
2. Spring Security + JWT 코드 전체 리딩 — `CustomSecurityConfig` → `JWTUtil` → 로그인 성공/실패 핸들러 → `JWTCheckFilter` → `CustomUserDetailsService` 순서로 함께 훑어보기로 예정되어 있었음
3. **게시판(자유게시판) 기능 구현** — 리스트(페이징) → 상세 → 등록 → 수정 순서 추천. 지금은 JS(`.jsx`)로 작업 (TS 마이그레이션 아직 착수 전, 4번 참고)
   - 🔧 **진행 중 (2026-09-18)**: `Board`/`BoardImage` 도메인 + `BoardRepository` + `BoardService`/`BoardServiceImpl` 완료 (5절 참고)
   - ⚠️ **테스트 재확인 필요 (2026-09-18)**: `./gradlew test`로 직접 재실행해보니 10개 중 `BoardRepositoryTest.delete()` 1개 실패(`NoSuchElementException`, `findById(1L)` 없음). 원인은 코드 버그가 아니라 **테스트가 `@Transactional` 롤백 없이 실제 로컬 DB에 커밋되는 구조라서, 같은 테스트 클래스를 반복 실행하면 auto-increment ID가 계속 밀려 하드코딩된 `1L`/`2L`/`3L`/`5L` 조회가 깨짐** — 다른 값으로 재시도하면 통과함. 이후 Repository/Service 테스트를 작성할 때 이 패턴(하드코딩 ID + 비격리 테스트)을 반복하면 계속 이런 식으로 헷갈릴 수 있음 — 여유 될 때 `@Transactional` 붙여서 테스트마다 롤백되게 하는 것 고려
   - **다음 순서**: `BoardController` 작성 → Postman으로 엔드포인트 직접 호출 검증(4절 API 검증 절차 3번) → 프론트 `.jsx` 연동 → Network 탭 확인(4절 4번) → 그 다음에 댓글·대댓글, 다중이미지 업로드 UI
   - ✅ **`global/domain/BaseEntity` 등록일/수정일 자동 기록 — 2026-09-18 완료**: `BackendApplication`에 `@EnableJpaAuditing` 추가 + `BaseEntity`에 `@Getter` 추가로 마무리. DB 직접 조회(`select board_number, regdate, moddate from tbl_board`)로 실제 타임스탬프 채워지는 것까지 확인함
   - ✅ **설계 결정 완료 (2026-09-18), 코드 반영은 진행 중** — `BoardDTO.memberEmail`을 `Member`가 아닌 `String`으로 변경 완료(테스트 코드도 맞춰 수정), `BoardService.modify`/`delete`에 `memberEmail` 파라미터 추가 + `AccessDeniedException`(반드시 `org.springframework.security.access` 패키지 것 사용 — `java.nio.file` 것 아님, 실수로 잘못 import하기 쉬움)으로 소유권 체크 완료. **아직 안 한 것**: `BoardController`에서 `Principal.getName()`으로 `memberEmail` 채우기, `insert()`를 `ModelMapper` 대신 `MemberRepository.findById()`로 직접 조회해 조립하는 방식으로 변경(2절 참고)
   - **다중 이미지 처리 결정 완료 (2026-09-18)**: `baby_project`(`CommunityPost`) 패턴을 참고해 게시글 수정과 이미지 추가/삭제를 별도 엔드포인트로 분리하기로 함 — 자세한 내용은 6절 ② 참고. 아직 미구현
4. **회원 기능 마무리** — 회원가입, 아이디 중복확인, 프로필사진 업로드, 회원탈퇴(소프트 삭제) — 14절 🔴 항목
5. 프론트엔드 TypeScript 마이그레이션 (본인이 직접 설정 예정) — **회원 기능까지 다 끝난 뒤에 진행하기로 결정**

참고: Member 엔티티는 지금 상태(스켈레톤 컨벤션, `@Column(nullable=false)` 없음) 유지로 확정. **게시판 엔티티(`Board`/`BoardImage`/`Comment`)부터는 `@Column(nullable=false)` 등 not-null 제약을 새로 적용하기로 결정** (Member엔 소급 적용 안 함, 단 2026-09-18 기준 아직 `Board`/`BoardImage`에도 미적용 — 실제로 걸지는 추후 판단).

## 14. 코드 리뷰 지적사항 (2026-09-09, 미해결 — 개발 원칙상 본인이 직접 수정)

Claude Code 세션에서 전체 코드 리뷰 진행. 빌드는 백엔드 `./gradlew compileJava` / 프론트 `vite build` 모두 통과. git 추적 상태 정상(`.idea/`·`.gradle/`·`upload/` 제외됨). 아래는 발견됐지만 **아직 안 고친 것들**.

### 🔴 회원 기능 — 기획서 ①은 "완료"였으나 실제로 없는 것 (6절 ① 갱신함) — 미해결
- 회원가입 엔드포인트 없음 (`MemberMapper.insert`는 있으나 호출부가 테스트 코드뿐)
- 아이디(이메일) 중복확인 엔드포인트 없음
- 프로필 사진 업로드 없음 — `Member` 도메인·`tbl_member` 스키마에 이미지 컬럼 없음, `CustomFileUtil` 미사용
- 회원탈퇴(소프트 삭제) 없음 — 스키마에 del 플래그 없음, 매퍼에 회원 delete 없음
- 프론트도 동일: `api/memberApi.js`에 `loginPost`/`modifyMember`만, 회원가입·중복확인 API·라우트 없음

### ✅ modify 흐름 버그 — 2026-09-15 해결
- `components/member/ModifyComponent.jsx` — pw 하드코딩(`"ABCD"`) 제거, 빈 문자열 기본값 + 안 바꿨으면 `pw` 필드 자체를 요청에서 제외(`{pw, ...rest} = member` 구조분해). `useEffect` 제거하고 `useState` lazy initializer로 대체, `handleChange` 불변성 위반 수정(`setMember({...member, [e.target.name]: e.target.value})`)
- `member/service/MemberServiceImpl.java` — `pw`가 `null`이면 `changePw()` 호출 스킵(NPE 방지), `changeSocial(false)`/`changeNickname`/`save` 중복 제거

### ✅ JWTCheckFilter — `/api/member/**` 전체 인증 스킵 — 2026-09-15 해결
- `security/filter/JWTCheckFilter.java` `shouldNotFilter()` — `/api/member/` 전체 스킵 대신 `/api/member/login`, `/api/member/refresh`만 명시적으로 스킵하도록 좁힘. `curl`로 직접 검증: `PUT /api/member/modify`에 토큰 없이 요청 시 이제 `{"error":"ERROR_ACCESS_TOKEN"}`으로 정상 차단됨

### 🟡 중간
- ✅ `security/controller/APIRefreshController.java:49` `(Integer)claims.get("exp")` — **2026-09-15 검증 완료, 9절 참고**: 현재 jjwt 0.11.5+Jackson 조합에서 안전함(2038년 전까지)
- ✅ `APIRefreshController` — **2026-09-15 해결**: `@RequestMapping` → `@PostMapping`으로 좁힘 (GET은 405로 차단 확인). refreshToken도 쿼리스트링 대신 body로 이동 — `util/jwtUtil.jsx`의 `refreshJWT`가 `URLSearchParams`로 body를 구성해 `axios.post(url, form, header)` 형태로 전송(`@RequestParam`은 그대로 둬도 form 형식 POST body를 자동으로 읽음). curl로 GET 차단·POST+body 동작 둘 다 검증 완료
- `global/dto/PageResponseDTO.java:53` `totalPage = pageNumList.size()` — 이건 "현재 블록의 페이지 수"지 전체 페이지 수가 아님. 게시판에서 전체 페이지 필요하면 `last` 값 별도 노출 — 미해결
- ~~`PageRequestDTO`에 MyBatis용 offset/limit(`getSkip()` 등) 필드 없음~~ — **JPA 전환으로 무효화됨**: MyBatis 수동 페이징 기준 지적이었음. JPA면 Spring Data의 `Pageable`/`PageRequest`를 쓰는 게 정석이라 접근 방식 자체가 달라짐. 게시판 착수 시점에 다시 판단
- ✅ `@Valid` 미적용 — **2026-09-15 해결**: `build.gradle`에 `spring-boot-starter-validation` 의존성 추가(3.0부터 `starter-web`에서 분리돼서 별도 추가 필요), `MemberModifyDTO`의 `email`/`nickname`에 `@NotBlank`(`pw`는 비번 안 바꾸는 경우 `null` 허용해야 해서 **의도적으로 제외**), `MemberController.modify()`에 `@Valid` 추가. curl로 검증: 빈 닉네임 → `CustomControllerAdvice`가 `"공백일 수 없습니다"` 에러로 차단, 정상 닉네임 → 통과, `pw` 없이도 정상 동작(NPE 방지 로직과 충돌 없음) 확인. **주의**: 새 의존성 추가는 devtools 핫리로드로 안 반영됨 — 서버 완전 재시작 필요했음

### 🟢 소소
- ✅ `global/util/CustomFileUtil.java:39` `mkdir()` → `mkdirs()`로 수정 완료 (2026-09-15)
- ✅ `CustomFileUtil.java:86` `winter.jpg` 폴백 — **2026-09-15 해결**: 처음엔 대체 이미지 파일(`siru.jpg`)로 바꾸는 방식을 시도했으나, `upload/`가 `.gitignore` 대상이라 다른 컴퓨터·평가자가 clone했을 때 그 파일이 없어 같은 문제가 재발한다는 걸 확인 → **최종적으로 파일 의존 없는 방식으로 변경**: 파일이 없으면 `ResponseEntity.notFound().build()`로 명확히 404 응답. 브라우저가 기본 "이미지 깨짐" 아이콘을 보여줌. "예쁜 기본 이미지" UX는 필요해지면 프론트엔드에서 `<img onError>`로 git 추적되는 정적 아이콘으로 교체하는 걸 권장(백엔드 파일 의존 없앰)
- ~~`schema.sql` `tbl_member_role`에 PK/UNIQUE 없음~~ — **JPA 전환으로 무효화됨**: `schema.sql` 자체가 삭제됨(2절 Persistence 의사결정 참고). 역할 저장은 이제 `@ElementCollection`이 자동 생성하는 `member_member_role_list` 테이블이 담당
- `CustomSecurityConfig.java:69` `allowedOriginPatterns("*")` + `allowCredentials(true)` → 프로덕션 브라우저는 거부. 로컬 데모는 무관 — 미해결

---
*이 문서는 Cowork(Claude)와의 기획·구현 대화를 정리한 인수인계 문서입니다. 프로젝트 루트에 `CLAUDE.md`로 저장해두면 Claude Code가 세션 시작 시 자동으로 읽어들여 컨텍스트로 활용합니다.*
