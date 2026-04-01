package Calendar.repository;

import Calendar.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link UserRepository}의 SQLite 구현체.
 * <p>
 * {@link DatabaseManager}를 통해 커넥션을 획득하고,
 * {@code users} 테이블을 대상으로 CRUD 작업을 수행한다.
 * </p>
 */
public class DbUserRepository implements UserRepository {

    /**
     * 새 유저를 DB에 저장한다.
     * <p>
     * 입력값 유효성을 검사한 뒤 {@code users} 테이블에 INSERT한다.
     * id가 중복되면 UNIQUE 제약 위반 메시지를 감지해 {@link IllegalArgumentException}으로 변환한다.
     * </p>
     *
     * @param user 저장할 유저 (null 불가, id·password 필수)
     * @throws IllegalArgumentException 유효성 검사 실패 또는 id 중복 시
     * @throws RuntimeException         DB 저장 실패 시
     */
    @Override
    public void addUser(User user) {
        if (user == null) throw new IllegalArgumentException("유저가 null일 수 없습니다.");
        if (user.getId() == null || user.getId().isBlank()) throw new IllegalArgumentException("유저 id는 필수입니다.");
        if (user.getPassword() == null || user.getPassword().isBlank()) throw new IllegalArgumentException("비밀번호는 필수입니다.");

        String sql = "INSERT INTO users (id, password, name) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                throw new IllegalArgumentException("이미 존재하는 id입니다: " + user.getId());
            }
            throw new RuntimeException("유저 저장 실패", e);
        }
    }

    /**
     * ID로 단일 유저를 조회한다.
     *
     * @param id 조회할 유저 ID
     * @return 해당 유저, 없으면 {@code null}
     * @throws RuntimeException DB 조회 실패 시
     */
    @Override
    public User findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapUser(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("유저 조회 실패", e);
        }
    }

    /**
     * 전체 유저 목록을 조회한다.
     *
     * @return 모든 유저의 불변 리스트 (없으면 빈 리스트)
     * @throws RuntimeException DB 조회 실패 시
     */
    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) users.add(mapUser(rs));
            return Collections.unmodifiableList(users);
        } catch (SQLException e) {
            throw new RuntimeException("유저 목록 조회 실패", e);
        }
    }

    /**
     * ID에 해당하는 유저를 삭제한다.
     *
     * @param id 삭제할 유저 ID
     * @throws IllegalArgumentException 해당 ID의 유저가 존재하지 않는 경우
     * @throws RuntimeException         DB 삭제 실패 시
     */
    @Override
    public void delete(String id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            int affected = pstmt.executeUpdate();
            if (affected == 0) throw new IllegalArgumentException("존재하지 않는 id입니다: " + id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("유저 삭제 실패", e);
        }
    }

    /**
     * {@link ResultSet}의 현재 행을 {@link User} 객체로 변환한다.
     *
     * @param rs 커서가 유효한 행을 가리키는 ResultSet
     * @return 변환된 {@link User} 객체
     * @throws SQLException 컬럼 접근 실패 시
     */
    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("id"),
                rs.getString("password"),
                rs.getString("name")
        );
    }
}
