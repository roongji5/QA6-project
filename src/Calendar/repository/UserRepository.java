package Calendar.repository;

import Calendar.model.User;
import java.util.List;

public interface UserRepository {
    void addUser(User user);
    User findById(String id);
    List<User> findAll();
    void delete(String id);
}
