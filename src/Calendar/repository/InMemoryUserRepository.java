package Calendar.repository;

import Calendar.model.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> users = new HashMap<>();

    @Override
    public void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("유저가 null일 수 없습니다.");
        }
        if (user.getId() == null || user.getId().isBlank()) {
            throw new IllegalArgumentException("유저 id는 필수입니다.");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        if (users.containsKey(user.getId())) {
            throw new IllegalArgumentException("이미 존재하는 id입니다: " + user.getId());
        }
        users.put(user.getId(), user);
    }

    @Override
    public User findById(String id) {
        return users.get(id);
    }

    @Override
    public List<User> findAll() {
        return Collections.unmodifiableList(List.copyOf(users.values()));
    }

    @Override
    public void delete(String id) {
        if (!users.containsKey(id)) {
            throw new IllegalArgumentException("존재하지 않는 id입니다: " + id);
        }
        users.remove(id);
    }
}
