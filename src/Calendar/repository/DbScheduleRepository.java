package Calendar.repository;

import Calendar.model.Schedule;

import java.sql.*;
import java.time.LocalDateTime;
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
     * @param schedule 저장할 일정 (null 불가, title·startDateTime·endDateTime 필수)
     * @throws IllegalArgumentException 유효성 검사 실패 시
     * @throws RuntimeException         DB 저장 실패 시
     */
    @Override
    public void addSchedule(Schedule schedule) {
        validateSchedule(schedule);

        String sql = "INSERT INTO schedules (user_id, title, start_datetime, end_datetime, description, color_hex, completed) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, schedule.getUserId());
            pstmt.setString(2, schedule.getTitle());
            pstmt.setString(3, schedule.getStartDateTime().toString());
            pstmt.setString(4, schedule.getEndDateTime().toString());
            pstmt.setString(5, schedule.getDescription());
            pstmt.setString(6, schedule.getColorHex());
            pstmt.setInt(7, schedule.isCompleted() ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("일정 저장 실패", e);
        }
    }

    @Override
    public void update(Schedule schedule) {
        validateSchedule(schedule);
        if (schedule.getId() <= 0) throw new IllegalArgumentException("유효한 일정 id가 필요합니다.");

        String sql = """
                UPDATE schedules
                SET title = ?, start_datetime = ?, end_datetime = ?, description = ?, color_hex = ?, completed = ?
                WHERE id = ? AND user_id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, schedule.getTitle());
            pstmt.setString(2, schedule.getStartDateTime().toString());
            pstmt.setString(3, schedule.getEndDateTime().toString());
            pstmt.setString(4, schedule.getDescription());
            pstmt.setString(5, schedule.getColorHex());
            pstmt.setInt(6, schedule.isCompleted() ? 1 : 0);
            pstmt.setInt(7, schedule.getId());
            pstmt.setLong(8, schedule.getUserId());

            int affected = pstmt.executeUpdate();
            if (affected == 0) throw new IllegalArgumentException("존재하지 않는 일정입니다: " + schedule.getId());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("일정 수정 실패", e);
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
        Schedule schedule = new Schedule(
                rs.getInt("id"),
                rs.getLong("user_id"),
                rs.getString("title"),
                LocalDateTime.parse(rs.getString("start_datetime")),
                LocalDateTime.parse(rs.getString("end_datetime")),
                rs.getString("description"),
                rs.getString("color_hex")
        );
        schedule.setCompleted(rs.getInt("completed") == 1);
        return schedule;
    }

    private void validateSchedule(Schedule schedule) {
        if (schedule == null) throw new IllegalArgumentException("일정이 null일 수 없습니다.");
        if (schedule.getUserId() <= 0) throw new IllegalArgumentException("유효한 사용자 id가 필요합니다.");
        if (schedule.getTitle() == null || schedule.getTitle().isBlank()) throw new IllegalArgumentException("일정 제목은 필수입니다.");
        if (schedule.getStartDateTime() == null) throw new IllegalArgumentException("일정 시작 일시는 필수입니다.");
        if (schedule.getEndDateTime() == null) throw new IllegalArgumentException("일정 종료 일시는 필수입니다.");
        if (schedule.getColorHex() == null || schedule.getColorHex().isBlank()) throw new IllegalArgumentException("일정 색상은 필수입니다.");
        if (!schedule.getStartDateTime().isBefore(schedule.getEndDateTime())) {
            throw new IllegalArgumentException("시작 일시는 종료 일시보다 빨라야 합니다.");
        }
    }
}
