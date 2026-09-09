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
| Frontend | React(Vite), TypeScript(신규 코드부터 적용 중 — 마이그레이션 진행 중), Redux Toolkit, Axios, Vanilla CSS/CSS Modules, `exifr`(EXIF 파싱), SunCalc(월령 계산) |
| Backend | JDK 21, Spring Boot 3.x, Spring Security + JWT(jjwt), MyBatis, MariaDB |
| AI | Python 3.10+, FastAPI, LangChain, ChromaDB(RAG), Ollama(로컬 LLM, 1순위) 또는 Groq(무료 API, 대안) |
| 외부 API | Kakao Map API, OpenWeatherMap(무료 티어 5일 예보 한도), TossPayments(결제 데모, 스트레치) |
| 툴 | Postman, Swagger UI |

**AI 엔진 관련 의사결정**: 원래 Unsloth 파인튜닝 + vLLM 서빙을 고려했으나, GPU 확보·데이터셋 구축·학습 사이클 등 1인 개발 일정에 리스크가 커서 Ollama/Groq + RAG 조합으로 변경. "독립 AI 백엔드 구축, RAG 파이프라인 설계, 도메인 특화 프롬프트 엔지니어링" 스토리는 유지하면서 인프라 리스크만 제거.

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

## 5. 현재 백엔드 패키지 구조 (실제 구현됨, 검증 완료)

```
com.backend
├── BackendApplication.java
│     @MapperScan(basePackages = "com.backend", annotationClass = Mapper.class)
├── global/                    ← 여러 기능이 공유하는 것
│   ├── config/                   CustomSecurityConfig, CustomServletConfig, RootConfig
│   ├── controller/advice/        CustomControllerAdvice
│   ├── controller/formatter/     LocalDateFormatter
│   ├── dto/                      PageRequestDTO, PageResponseDTO
│   └── util/                     CustomFileUtil
├── member/                    ← 회원 기능 전용
│   ├── controller/                MemberController
│   ├── domain/                    Member, MemberRole
│   ├── dto/                       MemberDTO, MemberModifyDTO
│   ├── mapper/                    MemberMapper (@Mapper 어노테이션 있음)
│   └── service/                   MemberService, MemberServiceImpl
└── security/                  ← 인증/인가 인프라
    ├── controller/                APIRefreshController
    ├── service/                   CustomUserDetailsService
    ├── filter/                    JWTCheckFilter
    ├── handler/                   APILoginFailHandler, APILoginSuccessHandler, CustomAccessDeniedHandler
    └── securityutil/              CustomJWTException, JWTUtil
```

새 매퍼 인터페이스를 추가할 때는 반드시 `@Mapper` 어노테이션을 붙일 것 (그래야 `@MapperScan`이 넓은 범위(`com.backend`)에서도 서비스 인터페이스 등과 혼동하지 않고 매퍼만 정확히 인식함 — 아래 8절 참고).

## 6. 핵심 기능 명세 (구현 순서: 회원 → 게시판 → 갤러리 → 야간출사지도 → AI서비스)

### ① 회원 및 인증 (MVP) — ✅ 구현 완료, GitHub 푸시 완료
- 페이지: 로그인 / 회원가입(아이디·비밀번호·닉네임 + 아이디 중복확인 + 프로필사진 1장 멀티파트) / 마이페이지(닉네임·프로필사진 수정, 회원탈퇴)
- JWT 기반 인증(BCrypt 암호화, Access/Refresh Token) — 기존 스켈레톤 코드 리딩으로 이해
- 회원탈퇴는 **소프트 삭제**: 탈퇴 후에도 게시글·사진은 유지, 작성자 표시만 "탈퇴한 회원" 등으로 대체
- 스트레치: 이메일 인증, 소셜 로그인, 관리자 페이지 회원관리
- 오늘 로그인/로그아웃/마이페이지 실제 동작 확인 완료

### ② 자유게시판 (MVP) — 다음 작업 대상, 미착수
- 페이지: 리스트(페이징) / 상세 / 등록(다중 이미지 업로드) / 수정(삭제 기능 포함)
- 댓글·대댓글은 2단계로 제한 (대댓글에는 답글 불가)
- 다중 이미지: `<input type="file" multiple>`로 동작 먼저 완성 → MyBatis `<selectKey>` + `<foreach>`로 `board_image` 테이블 일괄 INSERT
- 게시글 삭제 시 연관 이미지 파일·댓글도 함께 정리 (DB `ON DELETE CASCADE` 또는 애플리케이션 레벨)
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
2. **`@MapperScan("com.backend.mapper")` — 옛 패키지 경로**: 폴더 구조를 `global/member/security`로 재편하면서 `MemberMapper`가 `com.backend.member.mapper`로 이동했는데 스캔 경로를 안 맞춰서 매퍼 빈이 등록 안 됨(`NoSuchBeanDefinitionException`). `@MapperScan(basePackages = "com.backend", annotationClass = Mapper.class)`로 수정하고, `MemberMapper`에 `@Mapper` 어노테이션 추가. **주의**: `annotationClass` 없이 그냥 `@MapperScan("com.backend")`만 하면, `com.backend` 밑의 모든 인터페이스(예: `MemberService`)를 매퍼로 오인식해서 가짜 빈이 생기고 실제 서비스 빈과 충돌할 수 있음 — 그래서 `annotationClass = Mapper.class`로 필터링함. 새 매퍼 만들 때마다 `@Mapper` 잊지 말 것.
3. **MariaDB 계정 미생성**: `CREATE USER`/`CREATE DATABASE` SQL을 작성만 하고 실행을 안 해서, 앱 구동 시 존재하지 않는 계정으로 접속을 시도 → `GSS-API authentication exception` / `Unable to obtain Principal Name for authentication` 에러 발생. SQL을 실제로 실행해서 해결.
4. **한글 경로로 인한 Gradle 빌드/테스트 오류**: 프로젝트가 `OneDrive\바탕 화면\PhotoLog`(한글 경로 + OneDrive 동기화 폴더)에 있어서, `./gradlew clean test`를 해도 `ClassNotFoundException`이 반복 발생(컴파일은 성공하는데 테스트 워커 JVM이 클래스를 못 찾음). 콘솔에 한글 경로가 깨져서 출력되는 것도 방증. `C:\Users\hjc13\sai\PhotoLog`(영문 경로)로 프로젝트를 이동해서 해결. **앞으로 한글·OneDrive 경로는 피할 것.**

## 9. 코드 리뷰에서 발견했지만 의도적으로 그대로 둔 것들

- `APIRefreshController.checkTime((Integer)claims.get("exp"))` — jjwt 버전에 따라 `exp` claim이 `Long`으로 올 수 있어 `ClassCastException` 위험 잠재. 실제 토큰 재발급 테스트 시 확인 필요 (아직 미확인).
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
- 다른 컴퓨터에서 이어서 개발하려면: `git clone` → (frontend) `npm install` → (backend) IntelliJ로 열거나 `./gradlew build` → 로컬에 MariaDB 계정/DB 새로 생성(`application.properties`에 있는 계정정보 그대로 맞춰서) → `upload/` 폴더는 앱이 자동 생성.

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
- `schema.sql`: `tbl_member`, `tbl_member_role`만 유지 (cart/product/todo 테이블 제거)

## 13. 다음 할 일

1. **게시판(자유게시판) 기능 구현 시작** — 리스트(페이징) → 상세 → 등록 → 수정 순서 추천
2. Spring Security + JWT 코드 전체 리딩 (아직 미착수 — `CustomSecurityConfig` → `JWTUtil` → 로그인 성공/실패 핸들러 → `JWTCheckFilter` → `CustomUserDetailsService` 순서로 함께 훑어보기로 예정되어 있었음)
3. 프론트엔드 TypeScript 마이그레이션 진행 중 (본인이 직접 설정 중)

---
*이 문서는 Cowork(Claude)와의 기획·구현 대화를 정리한 인수인계 문서입니다. 프로젝트 루트에 `CLAUDE.md`로 저장해두면 Claude Code가 세션 시작 시 자동으로 읽어들여 컨텍스트로 활용합니다.*
