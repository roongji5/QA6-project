CREATE TABLE schedules_new (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    title       TEXT    NOT NULL,
    date        TEXT    NOT NULL,
    start_time  TEXT,
    end_time    TEXT,
    description TEXT,
    completed   INTEGER NOT NULL DEFAULT 0 CHECK (completed IN (0, 1)),
    FOREIGN KEY (user_id) REFERENCES users_new(id) ON DELETE CASCADE
);
