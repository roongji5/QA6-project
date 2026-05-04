package Calendar.model;

public class User {
    private long userID;
    private String loginID;
    private String password;
    private String name;

    public User(String loginID, String password, String name) {
        this.userID = 0;
        this.loginID = loginID;
        this.password = password;
        this.name = name;
    }

    public User(long userID, String loginID, String password, String name) {
        this.userID = userID;
        this.loginID = loginID;
        this.password = password;
        this.name = name;
    }

    public long getUserID() { return userID; }
    public String getLoginID() { return loginID; }
    public String getPassword() { return password; }
    public String getName() { return name; }

    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "[" + loginID + "] " + name;
    }
}
