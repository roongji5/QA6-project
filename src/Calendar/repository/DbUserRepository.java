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

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("id"),
                rs.getString("password"),
                rs.getString("name")
        );
    }
}
