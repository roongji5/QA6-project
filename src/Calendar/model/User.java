package Calendar.model;

public class User {
    private long userID;
    private String loginID;
    private String password;
    private String name;
    private String job;

    public User(String loginID, String password, String name) {
        this(loginID, password, name, null);
    }

    public User(String loginID, String password, String name, String job) {
        this.userID = 0;
        this.loginID = loginID;
        this.password = password;
        this.name = name;
        this.job = job;
    }

    public User(long userID, String loginID, String password, String name) {
        this(userID, loginID, password, name, null);
    }

    public User(long userID, String loginID, String password, String name, String job) {
        this.userID = userID;
        this.loginID = loginID;
        this.password = password;
        this.name = name;
        this.job = job;
    }

    public long getUserID() { return userID; }
    public String getLoginID() { return loginID; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getJob() { return job; }

    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setJob(String job) { this.job = job; }

    @Override
    public String toString() {
        String jobText = job == null || job.isBlank() ? "" : " (" + job + ")";
        return "[" + loginID + "] " + name + jobText;
    }
}
