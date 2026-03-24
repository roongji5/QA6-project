package Calendar.model;

public class User {
    private String id;
    private String password;
    private String name;

    public User(String id, String password, String name) {
        this.id = id;
        this.password = password;
        this.name = name;
    }

    // getters
    public String getId() { return id; }
    public String getPassword() { return password; }
    public String getName() { return name; }

    // setters
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "[" + id + "] " + name;
    }
}
