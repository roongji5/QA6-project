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
            stmt.execute("ALTER TABLE users ADD COLUMN job TEXT");
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
