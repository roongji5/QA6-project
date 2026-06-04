CREATE TABLE users_new (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    login_id TEXT    NOT NULL UNIQUE,
    password TEXT    NOT NULL,
    name     TEXT    NOT NULL,
    job      TEXT
);
