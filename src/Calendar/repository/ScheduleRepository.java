package Calendar.repository;

import Calendar.model.Schedule;
import java.util.List;

public interface ScheduleRepository {
    void addSchedule(Schedule schedule);
    //실패시 예외 발생 - void
    List<Schedule> getSchedules();
    Schedule findById(int id);
    List<Schedule> findByUserId(long userId);
    void update(Schedule schedule);
    void delete(int id);
}
