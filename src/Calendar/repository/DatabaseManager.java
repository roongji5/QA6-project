package Calendar.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite 데이터베이스 연결 및 초기화를 담당하는 유틸리티 클래스.
 * <p>
 * 애플리케이션 시작 시 {@link #initialize()}를 호출하여 테이블을 생성하고,
 * 이후 {@link #getConnection()}으로 커넥션을 획득해 사용한다.
 * </p>
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:calendar.db";

    /**
     * SQLite DB에 대한 새 {@link Connection}을 반환한다.
     *
     * @return 데이터베이스 커넥션
     * @throws SQLException 연결에 실패한 경우
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * 애플리케이션 구동에 필요한 테이블을 생성한다.
     * <p>
     * 이미 테이블이 존재하면 아무 작업도 수행하지 않는다 ({@code CREATE TABLE IF NOT EXISTS}).
     * 생성 대상 테이블: {@code users}, {@code schedules}
     * </p>
     *
     * @throws RuntimeException DDL 실행 중 {@link SQLException}이 발생한 경우
     */
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
