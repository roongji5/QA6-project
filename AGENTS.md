# Repository Guidelines

---

## Grand Principles

- Think before coding: understand the request, read the relevant code, and identify risks before editing.
- Simplicity first: prefer the smallest clear solution that satisfies the current task.
- Make surgical changes: touch only the files and behavior needed for the task.
- Keep every action tied to the user's requested outcome, then verify the result with the most focused relevant check.
- Preserve the existing application behavior unless the requested change explicitly requires a behavior change.
- Do not rewrite working code only for style preferences.
- Treat user changes as intentional. Never revert or overwrite work you did not create.
- Prefer simple Java and Swing solutions over new frameworks or broad abstractions.

## Overview

- This repository contains a Java 21 desktop calendar and schedule management application.
- The UI is built with Java Swing.
- Persistence is handled locally with SQLite through JDBC.
- The main entry point is `Calendar.app.CalendarApp`.
- Current features include login, sign-up, monthly calendar display, schedule management, session-based user info, and user info editing.

## Project Structure

- `src/Calendar/app`: application entry point and startup wiring.
- `src/Calendar/model`: domain models such as `User` and `Schedule`.
- `src/Calendar/repository`: database connection logic and repository implementations.
- `src/Calendar/session`: current logged-in user state.
- `src/Calendar/ui`: Swing frames, panels, and dialogs.
- `src/resources/sql`: schema and database reset SQL files.
- `lib`: local third-party JAR files, including the SQLite JDBC driver.
- `out`: generated build output. Do not edit manually.

## Non-Negotiables

- Do not commit or rely on generated local database files such as `calendar.db` or `identifier.sqlite`.
- Do not access the database directly from Swing UI classes. Use repository classes.
- Do not add network calls, external services, or hidden background behavior without explicit user approval.
- Do not log passwords or sensitive user information.
- Do not block the Swing Event Dispatch Thread with long-running work.
- Do not introduce a new dependency unless it is clearly needed and approved.
- Do not change public behavior silently. If behavior changes, state it clearly.

## Required Workflow

- Inspect relevant files before changing them.
- Identify the smallest set of files needed for the requested change.
- Make edits in place and preserve unrelated code.
- Compile or run the affected flow when practical.
- Report what changed, what was verified, and what could not be verified.
- If requirements are ambiguous, make a reasonable assumption only when the risk is low. Otherwise, ask the user.

## Code Quality

- Use standard Java naming conventions:
  - Classes: `PascalCase`
  - Methods and variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- Extract helper methods when UI construction or database logic becomes hard to read.
- Use `private` visibility by default.
- Validate user input before saving data.
- Keep comments brief and useful. Avoid comments that only repeat the code.
- Keep Korean and English UI text consistent with the existing screen being edited.

## UI And Domain Boundaries

- UI classes should focus on screen construction, event wiring, and user-facing messages.
- Do not duplicate rules for adding, updating, or deleting schedules across UI classes.
- Move repeated schedule behavior into service or repository flows when appropriate.
- Repository classes should focus on SQL execution and object mapping.
- Do not access SQLite directly from Swing UI classes.

## Persistence

- Use `DatabaseManager` for database initialization and connection-related setup.
- Use prepared statements for any user-provided values.
- Close JDBC resources with try-with-resources whenever possible.
- Keep model objects simple and predictable.
- When updating user information, update both SQLite and `UserSession` so the UI sees the latest data.
- Handle missing session data gracefully.
- Use repository interfaces and implementations for data access.

## Migrations

- Keep base schema definitions in `src/resources/sql/create_tables.sql`.
- Keep destructive reset/drop SQL in `src/resources/sql/drop_tables.sql`.
- For schema changes, prefer an explicit migration path that preserves existing local data when practical.
- Do not delete or replace user data as part of a migration unless the user explicitly asks for a reset.
- Make migration SQL idempotent when possible.
- Document any manual migration step that the user must run.
- TBD: final team policy for versioned migration file naming.

## External Integrations

- The project currently depends on the local SQLite JDBC JAR in `lib`.
- Avoid adding package managers, build tools, APIs, cloud services, or remote integrations without user approval.
- If an external dependency is necessary, explain why it is needed and how it affects local setup.
- Keep the application runnable in a local desktop environment.

## Logging And Observability

- Use simple console output only when it helps local debugging or startup diagnosis.
- Do not print passwords, raw credentials, or unnecessary personal data.
- Prefer clear user-facing dialogs for recoverable UI errors.
- Repository errors should provide enough context to debug without exposing sensitive values.


## Testing

- Compile after code changes when practical.
- Manually verify affected Swing flows when automated tests are not available.
- For persistence changes, verify schema creation and repository behavior.
- For session-sensitive changes, verify behavior before and after login state changes.
- Mention any test or manual verification that could not be completed.
- If verification cannot be run, report the exact check that was skipped and why.

## Local Runtime

- Java version: Java 21.
- Register `lib/sqlite-jdbc-3.51.3.0.jar` as a project library when running from an IDE.
- Run the application from `Calendar.app.CalendarApp`.
- The local SQLite database is created in the project directory when the app runs.
- Generated files under `out` should be treated as build output.
- TBD: standard command-line compile/run command for the team.

## Git And PR

- Keep commits small and task-focused.
- Stage only files related to the current task.
- Do not stage or commit unrelated modified files.
- Do not create commits, push, bump versions, or open PRs unless the user asks.
- Do not edit IDE metadata unless necessary for the requested change.
- Before finalizing, check `git status` and identify files changed by the agent.

Commit messages in this repo follow this format:

- `:gitmoji: type: message`

Examples:

- `:bug: fix: prevent duplicate schedule save`
- `:sparkles: feat: add user profile edit dialog`
- `:memo: docs: tighten agent guidelines`

PR or final summaries should mention:

- Behavior changed
- UI impact
- Database impact
- Login/session/security impact
- External integration impact
- Verification performed
- Known risks or skipped checks

## Agent Checklist

- I read the relevant files before editing.
- I kept the change scoped to the request.
- I preserved unrelated user changes.
- I used repository classes for persistence changes.
- I kept Swing work consistent with existing UI patterns.
- I considered login/session behavior where user data is involved.
- I avoided committing generated database and build files.
- I compiled or manually verified the affected flow when practical.
- I reported changed files and verification results clearly.
