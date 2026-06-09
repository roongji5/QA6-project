package Calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record RecommendedSchedule(String title, LocalDateTime startDateTime, LocalDateTime endDateTime,
                                  String description) {

    public static RecommendedSchedule forJob(String job, LocalDate date) {
        if ("학생".equals(job)) {
            return new RecommendedSchedule(
                    "공부",
                    LocalDateTime.of(date, LocalTime.of(19, 0)),
                    LocalDateTime.of(date, LocalTime.of(22, 0)),
                    "오늘 저녁 공부 시간을 확보해보세요."
            );
        }
        if ("회사원".equals(job)) {
            return new RecommendedSchedule(
                    "회의",
                    LocalDateTime.of(date, LocalTime.of(14, 0)),
                    LocalDateTime.of(date, LocalTime.of(15, 0)),
                    "오후 업무 공유 회의를 진행해보세요."
            );
        }
        if ("자영업자".equals(job)) {
            return new RecommendedSchedule(
                    "오픈준비",
                    LocalDateTime.of(date, LocalTime.of(9, 0)),
                    LocalDateTime.of(date, LocalTime.of(10, 0)),
                    "영업 시작 전 오픈 준비 시간을 챙겨보세요."
            );
        }
        return null;
    }
}
