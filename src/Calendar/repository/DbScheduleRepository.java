package Calendar.repository;

import Calendar.model.Schedule;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link ScheduleRepository}의 SQLite 구현체.
 * <p>
 * {@link DatabaseManager}를 통해 커넥션을 획득하고,
 * {@code schedules} 테이블을 대상으로 CRUD 작업을 수행한다.
 * </p>
 */
public class DbScheduleRepository implements ScheduleRepository {

    /**
     * 새 일정을 DB에 저장한다.
     * <p>
     * 입력값 유효성을 검사한 뒤 {@code schedules} 테이블에 INSERT한다.
     * </p>
     *
     * @param schedule 저장할 일정 (null 불가, title·date 필수)
     * @throws IllegalArgumentException 유효성 검사 실패 시
     * @throws RuntimeException         DB 저장 실패 시
     */
    @Override
    public void addSchedule(Schedule schedule) {
        if (schedule == null) throw new IllegalArgumentException("일정이 null일 수 없습니다.");
        if (schedule.getUserId() <= 0) throw new IllegalArgumentException("유효한 사용자 id가 필요합니다.");
        if (schedule.getTitle() == null || schedule.getTitle().isBlank()) throw new IllegalArgumentException("일정 제목은 필수입니다.");
        if (schedule.getDate() == null) throw new IllegalArgumentException("일정 날짜는 필수입니다.");
        if (schedule.getStartTime() != null && schedule.getEndTime() != null
                && !schedule.getStartTime().isBefore(schedule.getEndTime())) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 빨라야 합니다.");
        }

        String sql = "INSERT INTO schedules (user_id, title, date, start_time, end_time, description, completed) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, schedule.getUserId());
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

    /**
     * 모든 일정을 조회한다.
     *
     * @return 전체 일정의 불변 리스트 (데이터 없으면 빈 리스트)
     * @throws RuntimeException DB 조회 실패 시
     */
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

    /**
     * ID로 단일 일정을 조회한다.
     *
     * @param id 조회할 일정의 PK
     * @return 해당 일정, 없으면 {@code null}
     * @throws RuntimeException DB 조회 실패 시
     */
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

    /**
     * 특정 유저의 일정 목록을 조회한다.
     *
     * @param userId 조회할 유저 ID
     * @return 해당 유저의 일정 불변 리스트 (없으면 빈 리스트)
     * @throws RuntimeException DB 조회 실패 시
     */
    @Override
    public List<Schedule> findByUserId(long userId) {
        String sql = "SELECT * FROM schedules WHERE user_id = ?";
        List<Schedule> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapSchedule(rs));
            return Collections.unmodifiableList(list);
        } catch (SQLException e) {
            throw new RuntimeException("유저 일정 조회 실패", e);
        }
    }

    /**
     * ID에 해당하는 일정을 삭제한다.
     *
     * @param id 삭제할 일정의 PK
     * @throws IllegalArgumentException 해당 ID의 일정이 존재하지 않는 경우
     * @throws RuntimeException         DB 삭제 실패 시
     */
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

    /**
     * {@link ResultSet}의 현재 행을 {@link Schedule} 객체로 변환한다.
     *
     * @param rs 커서가 유효한 행을 가리키는 ResultSet
     * @return 변환된 {@link Schedule} 객체
     * @throws SQLException 컬럼 접근 실패 시
     */
    private Schedule mapSchedule(ResultSet rs) throws SQLException {
        String startTimeStr = rs.getString("start_time");
        String endTimeStr = rs.getString("end_time");
        Schedule schedule = new Schedule(
                rs.getInt("id"),
                rs.getLong("user_id"),
                rs.getString("title"),
                LocalDate.parse(rs.getString("date")),
                startTimeStr != null ? LocalTime.parse(startTimeStr) : null,
                endTimeStr != null ? LocalTime.parse(endTimeStr) : null,
                rs.getString("description")
        );
        schedule.setCompleted(rs.getInt("completed") == 1);
        return schedule;
    }
}
