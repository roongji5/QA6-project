package Calendar.model;

import java.time.LocalDateTime;

public class Schedule {
    public static final String DEFAULT_COLOR_HEX = "#5470E0";

    private int id;
    private long userId;
    private String title;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String description;
    private String colorHex;
    private boolean completed;

    public Schedule(int id, long userId, String title, LocalDateTime startDateTime, LocalDateTime endDateTime,
            String description) {
        this(id, userId, title, startDateTime, endDateTime, description, DEFAULT_COLOR_HEX);
    }

    public Schedule(int id, long userId, String title, LocalDateTime startDateTime, LocalDateTime endDateTime,
            String description, String colorHex) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.description = description;
        this.colorHex = colorHex == null || colorHex.isBlank() ? DEFAULT_COLOR_HEX : colorHex;
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

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public String getDescription() {
        return description;
    }

    public String getColorHex() {
        return colorHex;
    }

    public boolean isCompleted() {
        return completed;
    }

    // setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex == null || colorHex.isBlank() ? DEFAULT_COLOR_HEX : colorHex;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        String status = completed ? "[완료]" : "[ ]";
        return status + " " + title + " | " + startDateTime + "~" + endDateTime;
    }
}
