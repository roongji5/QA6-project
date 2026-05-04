package Calendar.model;

import java.time.LocalTime;
import java.time.LocalDate;

public class Schedule {
    private int id;
    private long userId;
    private String title;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String description;
    private boolean completed;

    public Schedule(int id, long userId, String title, LocalDate date, LocalTime startTime, LocalTime endTime,
            String description) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.completed = false;
    }

    // getters
    public int getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    // setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        String status = completed ? "[완료]" : "[ ]";
        return status + " " + title + " | " + date + " " + startTime + "~" + endTime;
    }
}
