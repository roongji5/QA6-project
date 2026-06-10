# QA6-project

Calendar schedule management project built with Java Swing and SQLite.

## Team Members
- 주지훈
- 공수민
- 백지환

## Tech Stack

- Java 21
- Java Swing
- SQLite
- JDBC

## Current Features

- Login / Sign-up UI
- Monthly calendar view
- Date selection and empty schedule creation window
- Session-based user info view/edit
- SQLite-based user/schedule repositories

## Structure

```text
src/Calendar/
├── app/          # Application entry point
├── model/        # User and Schedule models
├── repository/   # DB connection and repositories
├── session/      # Login session management
└── ui/           # Swing screens
```

## How to Run
1 Register `lib/sqlite-jdbc-3.51.3.0.jar` as a library.
2 Run `Calendar.app.CalendarApp`.

## DB

- The DB file is created as `calendar.db` when the app runs.
- Table creation SQL is in `src/resources/sql/create_tables.sql`.
- Local DB files are not committed to Git.
