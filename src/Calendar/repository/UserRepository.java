package Calendar.repository;

import Calendar.model.User;
import java.util.List;

public interface UserRepository {
    void addUser(User user);
    User findById(Long id);
    User findByLoginId(String loginId);
    List<User> findAll();
    void update(User user);
    void delete(Long id);
}
