# QA6-project

Java와 SQLite를 사용한 간단한 캘린더 일정 관리 프로젝트입니다.

## 프로젝트 개요

이 프로젝트는 사용자 정보를 저장하고, 사용자별 일정을 등록/조회/삭제하는 기능을 목표로 합니다.
현재는 콘솔 실행용 테스트 코드와 SQLite 기반 Repository 계층이 중심입니다.

## 기술 스택

- Java 21
- SQLite
- JDBC

## 프로젝트 구조

```text
QA6-project/
├── lib/
│   └── sqlite-jdbc-3.51.3.0.jar
├── src/
│   ├── Calendar/
│   │   ├── app/
│   │   │   └── TestMain.java
│   │   ├── model/
│   │   │   ├── Schedule.java
│   │   │   └── User.java
│   │   └── repository/
│   │       ├── DatabaseManager.java
│   │       ├── DbScheduleRepository.java
│   │       ├── DbUserRepository.java
│   │       ├── ScheduleRepository.java
│   │       └── UserRepository.java
│   └── resources/
│       └── sql/
│           ├── create_tables.sql
│           └── drop_tables.sql
└── README.md
```

## 주요 클래스

- `Calendar.app.TestMain`
  - 프로젝트 동작을 확인하기 위한 콘솔 실행 클래스입니다.
- `Calendar.model.User`
  - 사용자 정보를 표현하는 모델 클래스입니다.
- `Calendar.model.Schedule`
  - 일정 정보를 표현하는 모델 클래스입니다.
- `Calendar.repository.DatabaseManager`
  - SQLite 연결과 테이블 초기화를 담당합니다.
- `Calendar.repository.DbUserRepository`
  - 사용자 데이터 저장/조회/수정/삭제를 담당합니다.
- `Calendar.repository.DbScheduleRepository`
  - 일정 데이터 저장/조회/삭제를 담당합니다.
- `src/resources/sql/create_tables.sql`
  - 애플리케이션에서 사용하는 테이블 생성 SQL입니다.
- `src/resources/sql/drop_tables.sql`
  - 테이블 삭제 SQL입니다.

## DB 스키마 방향

사용자는 내부 식별자인 숫자 PK와 로그인용 ID를 분리합니다.
일정은 사용자의 숫자 PK를 외래키로 참조합니다.

```sql
CREATE TABLE users (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    login_id TEXT    NOT NULL UNIQUE,
    password TEXT    NOT NULL,
    name     TEXT    NOT NULL,
    job      TEXT
);

CREATE TABLE schedules (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    title       TEXT    NOT NULL,
    date        TEXT    NOT NULL,
    start_time  TEXT,
    end_time    TEXT,
    description TEXT,
    completed   INTEGER NOT NULL DEFAULT 0 CHECK (completed IN (0, 1)),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

## 실행 방법

현재 프로젝트는 Gradle이나 Maven 없이 IntelliJ IDEA 모듈 설정과 JDBC jar 파일을 직접 사용하는 구조입니다.

1. IntelliJ IDEA에서 프로젝트를 엽니다.
2. `lib/sqlite-jdbc-3.51.3.0.jar`가 라이브러리로 등록되어 있는지 확인합니다.
3. `src/Calendar/app/TestMain.java`를 실행합니다.

## DB 초기화 참고

- `DatabaseManager.initialize()`는 테이블 생성 후 기존 `users` 테이블에 `job` 컬럼이 없으면 자동으로 추가합니다.
- SQLite의 `CREATE TABLE IF NOT EXISTS`는 이미 만들어진 테이블 구조를 자동으로 바꾸지 않으므로, 이후 큰 스키마 변경에는 별도 마이그레이션 관리가 필요합니다.

## 다음 작업 후보

1. DB 마이그레이션 관리 체계 도입
2. Gradle 또는 Maven 도입
3. 테스트 코드 추가
4. Service 계층과 UI 계층 구현
