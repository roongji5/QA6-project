package Calendar.app;

import Calendar.model.Schedule;
import Calendar.model.User;
import Calendar.repository.DatabaseManager;
import Calendar.repository.DbScheduleRepository;
import Calendar.repository.DbUserRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TestMain {

    public static void main(String[] args) {
        DatabaseManager.initialize();

        DbUserRepository userRepo = new DbUserRepository();
        DbScheduleRepository scheduleRepo = new DbScheduleRepository();

        // 유저 저장
        User user = new User("test1", "pw1234", "홍길동");
        userRepo.addUser(user);
        User savedUser = userRepo.findByLoginId("test1");
        System.out.println("유저 저장: " + savedUser);

        // 일정 저장
        Schedule schedule = new Schedule(0, savedUser.getUserID(), "팀 회의", LocalDate.of(2026, 4, 1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), "주간 회의");
        scheduleRepo.addSchedule(schedule);

        List<Schedule> list = scheduleRepo.findByUserId(savedUser.getUserID());
        System.out.println("일정 저장 확인 (" + list.size() + "건):");
        list.forEach(System.out::println);
    }
}
