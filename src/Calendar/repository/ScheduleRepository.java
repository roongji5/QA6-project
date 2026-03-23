package Calendar.repository;

import Calendar.model.Schedule;
import java.util.List;

public interface ScheduleRepository {
    void addSchedule(Schedule schedule);
    List<Schedule> getSchedules();
    void delete(int id);
}
