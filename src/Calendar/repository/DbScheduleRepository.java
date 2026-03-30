package Calendar.repository;

import Calendar.model.Schedule;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DbScheduleRepository implements ScheduleRepository {

    @Override
    public void addSchedule(Schedule schedule) {
        if (schedule == null) throw new IllegalArgumentException("일정이 null일 수 없습니다.");
        if (schedule.getTitle() == null || schedule.getTitle().isBlank()) throw new IllegalArgumentException("일정 제목은 필수입니다.");
        if (schedule.getDate() == null) throw new IllegalArgumentException("일정 날짜는 필수입니다.");
        if (schedule.getStartTime() != null && schedule.getEndTime() != null
                && !schedule.getStartTime().isBefore(schedule.getEndTime())) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 빨라야 합니다.");
        }

        String sql = "INSERT INTO schedules (user_id, title, date, start_time, end_time, description, completed) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, schedule.getUserId());
            pstmt.setString(2, schedule.getTitle());
            pstmt.setString(3, schedule.getDate().toString());
            pstmt.setString(4, schedule.getStartTime() != null ? schedule.getStartTime().toString() : null);
            pstmt.setString(5, schedule.getEndTime() != null ? schedule.getEndTime().toString() : null);
            pstmt.setString(6, schedule.getDescription());
            pstmt.setInt(7, schedule.isCompleted() ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("일정 저장 실패", e);
        }
    }

    @Override
    public List<Schedule> getSchedules() {
        String sql = "SELECT * FROM schedules";
        List<Schedule> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapSchedule(rs));
            return Collections.unmodifiableList(list);
        } catch (SQLException e) {
            throw new RuntimeException("일정 목록 조회 실패", e);
        }
    }

    @Override
    public Schedule findById(int id) {
        String sql = "SELECT * FROM schedules WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapSchedule(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("일정 조회 실패", e);
        }
    }

    @Override
    public List<Schedule> findByUserId(String userId) {
        String sql = "SELECT * FROM schedules WHERE user_id = ?";
        List<Schedule> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapSchedule(rs));
            return Collections.unmodifiableList(list);
        } catch (SQLException e) {
            throw new RuntimeException("유저 일정 조회 실패", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM schedules WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affected = pstmt.executeUpdate();
            if (affected == 0) throw new IllegalArgumentException("존재하지 않는 id입니다: " + id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("일정 삭제 실패", e);
        }
    }

    private Schedule mapSchedule(ResultSet rs) throws SQLException {
        String startTimeStr = rs.getString("start_time");
        String endTimeStr = rs.getString("end_time");
        return new Schedule(
                rs.getInt("id"),
                rs.getString("user_id"),
                rs.getString("title"),
                LocalDate.parse(rs.getString("date")),
                startTimeStr != null ? LocalTime.parse(startTimeStr) : null,
                endTimeStr != null ? LocalTime.parse(endTimeStr) : null,
                rs.getString("description")
        );
    }
}
