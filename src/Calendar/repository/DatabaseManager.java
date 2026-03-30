package Calendar.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:calendar.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initialize() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id       TEXT PRIMARY KEY,
                    password TEXT NOT NULL,
                    name     TEXT NOT NULL
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS schedules (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id     TEXT NOT NULL,
                    title       TEXT NOT NULL,
                    date        TEXT NOT NULL,
                    start_time  TEXT,
                    end_time    TEXT,
                    description TEXT,
                    completed   INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);
        } catch (SQLException e) {
            throw new RuntimeException("DB 초기화 실패", e);
        }
    }
}
