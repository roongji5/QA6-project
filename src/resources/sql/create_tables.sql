CREATE TABLE IF NOT EXISTS users (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    login_id TEXT    NOT NULL UNIQUE,
    password TEXT    NOT NULL,
    name     TEXT    NOT NULL,
    job      TEXT
);

CREATE TABLE IF NOT EXISTS schedules (
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
