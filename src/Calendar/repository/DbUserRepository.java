package Calendar.repository;

import Calendar.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DbUserRepository implements UserRepository {

    @Override
    public void addUser(User user) {
        if (user == null) throw new IllegalArgumentException("유저가 null일 수 없습니다.");
        if (user.getLoginID() == null || user.getLoginID().isBlank()) throw new IllegalArgumentException("login_id는 필수입니다.");
        if (user.getPassword() == null || user.getPassword().isBlank()) throw new IllegalArgumentException("password는 필수입니다.");
        if (user.getName() == null || user.getName().isBlank()) throw new IllegalArgumentException("name은 필수입니다.");

        String sql = "INSERT INTO users (login_id, password, name) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getLoginID());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("유저 저장 실패", e);
        }
    }

    @Override
    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapUser(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("유저 조회 실패", e);
        }
    }

    @Override
    public User findByLoginId(String loginId) {
        String sql = "SELECT * FROM users WHERE login_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loginId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapUser(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("유저 조회 실패", e);
        }
    }

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

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET login_id = ?, password = ?, name = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getLoginID());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getName());
            pstmt.setLong(4, user.getUserID());
            int affected = pstmt.executeUpdate();
            if (affected == 0) throw new IllegalArgumentException("존재하지 않는 사용자입니다. id: " + user.getUserID());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("유저 수정 실패", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            int affected = pstmt.executeUpdate();
            if (affected == 0) throw new IllegalArgumentException("존재하지 않는 사용자입니다. id: " + id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("유저 삭제 실패", e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("login_id"),
                rs.getString("password"),
                rs.getString("name")
        );
    }
}
