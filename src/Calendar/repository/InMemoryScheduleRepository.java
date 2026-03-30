package Calendar.repository;

import Calendar.model.Schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryScheduleRepository implements ScheduleRepository {

    private final List<Schedule> schedules = new ArrayList<>();

    // 저장 규칙 1: null 일정 추가 불가
    // 저장 규칙 2: title이 null이거나 빈 문자열이면 추가 불가
    // 저장 규칙 3: date가 null이면 추가 불가
    // 저장 규칙 4: startTime이 endTime보다 늦으면 추가 불가
    // 저장 규칙 5: 동일한 id를 가진 일정은 중복 추가 불가
    @Override
    public void addSchedule(Schedule schedule) {
        if (schedule == null) {
            throw new IllegalArgumentException("일정이 null일 수 없습니다.");
        }
        if (schedule.getTitle() == null || schedule.getTitle().isBlank()) {
            throw new IllegalArgumentException("일정 제목은 필수입니다.");
        }
        if (schedule.getDate() == null) {
            throw new IllegalArgumentException("일정 날짜는 필수입니다.");
        }
        if (schedule.getStartTime() != null && schedule.getEndTime() != null
                && !schedule.getStartTime().isBefore(schedule.getEndTime())) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 빨라야 합니다.");
        }
        for (Schedule s : schedules) {
            if (s.getId() == schedule.getId()) {
                throw new IllegalArgumentException("이미 존재하는 id입니다: " + schedule.getId());
            }
        }
        schedules.add(schedule);
    }

    // 외부에서 리스트를 직접 수정하지 못하도록 unmodifiableList 반환
    @Override
    public List<Schedule> getSchedules() {
        return Collections.unmodifiableList(schedules);
    }

    //findById는 없으면 null 반환, findByUserId는 해당 유저의 일정 리스트 반환
    @Override
    public Schedule findById(int id) {
        for (Schedule s : schedules) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    @Override
    public List<Schedule> findByUserId(String userId) {
        List<Schedule> result = new ArrayList<>();
        for (Schedule s : schedules) {
            if (s.getUserId().equals(userId)) result.add(s);
        }
        return Collections.unmodifiableList(result);
    }

    // 존재하지 않는 id 삭제 시 예외 발생
    @Override
    public void delete(int id) {
        boolean removed = schedules.removeIf(s -> s.getId() == id);
        if (!removed) {
            throw new IllegalArgumentException("존재하지 않는 id입니다: " + id);
        }
    }
}
