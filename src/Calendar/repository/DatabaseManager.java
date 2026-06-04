package Calendar.repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:calendar.db";
    private static final String CREATE_TABLES_SQL = "sql/create_tables.sql";
    private static final String DROP_TABLES_SQL = "sql/drop_tables.sql";
    private static final String ADD_USERS_JOB_COLUMN_SQL = "sql/add_users_job_column.sql";
    private static final String CREATE_LEGACY_USERS_BACKUP_SQL = "sql/migration/create_legacy_users_backup.sql";
    private static final String COPY_LEGACY_USERS_SQL = "sql/migration/copy_legacy_users.sql";
    private static final String CREATE_LEGACY_SCHEDULES_BACKUP_SQL = "sql/migration/create_legacy_schedules_backup.sql";
    private static final String COPY_LEGACY_SCHEDULES_SQL = "sql/migration/copy_legacy_schedules.sql";

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            conn.close();
            throw e;
        }
        return conn;
    }

    public static void initialize() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            migrateLegacyUsersTableIfNeeded(stmt);
            executeSqlScript(stmt, CREATE_TABLES_SQL);
            ensureUsersJobColumn(stmt);
        } catch (SQLException | IOException e) {
            throw new RuntimeException("DB 초기화 실패", e);
        }
    }

    public static void dropTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            executeSqlScript(stmt, DROP_TABLES_SQL);
        } catch (SQLException | IOException e) {
            throw new RuntimeException("DB 테이블 삭제 실패", e);
        }
    }

    private static void executeSqlScript(Statement stmt, String resourcePath) throws SQLException, IOException {
        String script = readSqlScript(resourcePath);
        for (String sql : script.split(";")) {
            String trimmedSql = sql.trim();
            if (!trimmedSql.isEmpty()) {
                stmt.execute(trimmedSql);
            }
        }
    }

    private static void ensureUsersJobColumn(Statement stmt) throws SQLException {
        if (!hasColumn(stmt, "users", "job")) {
            try {
                executeSqlScript(stmt, ADD_USERS_JOB_COLUMN_SQL);
            } catch (IOException e) {
                throw new SQLException("users.job 컬럼 추가 SQL 로드 실패", e);
            }
        }
    }

    private static void migrateLegacyUsersTableIfNeeded(Statement stmt) throws SQLException, IOException {
        if (!hasTable(stmt, "users") || hasColumn(stmt, "users", "login_id")) {
            return;
        }

        stmt.execute("PRAGMA foreign_keys = OFF");
        try {
            stmt.execute("BEGIN TRANSACTION");
            executeSqlScript(stmt, CREATE_LEGACY_USERS_BACKUP_SQL);
            executeSqlScript(stmt, COPY_LEGACY_USERS_SQL);

            if (hasTable(stmt, "schedules")) {
                executeSqlScript(stmt, CREATE_LEGACY_SCHEDULES_BACKUP_SQL);
                executeSqlScript(stmt, COPY_LEGACY_SCHEDULES_SQL);
                stmt.execute("DROP TABLE schedules");
            }

            stmt.execute("DROP TABLE users");
            stmt.execute("ALTER TABLE users_new RENAME TO users");
            if (hasTable(stmt, "schedules_new")) {
                stmt.execute("ALTER TABLE schedules_new RENAME TO schedules");
            }
            stmt.execute("COMMIT");
        } catch (SQLException e) {
            stmt.execute("ROLLBACK");
            throw e;
        } finally {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
    }

    private static boolean hasTable(Statement stmt, String tableName) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type = 'table' AND name = '" + tableName + "'";
        try (ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next();
        }
    }

    private static boolean hasColumn(Statement stmt, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (rs.next()) {
                if (columnName.equals(rs.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }

    private static String readSqlScript(String resourcePath) throws IOException {
        InputStream resourceStream = DatabaseManager.class.getClassLoader().getResourceAsStream(resourcePath);
        if (resourceStream == null) {
            resourceStream = DatabaseManager.class.getClassLoader().getResourceAsStream("resources/" + resourcePath);
        }
        if (resourceStream != null) {
            try (InputStream in = resourceStream) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        Path filePath = Path.of("src", "resources", resourcePath);
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }
}
