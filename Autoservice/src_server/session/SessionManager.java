package session;

import model.User;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final Map<String, User> tokenToUser = new ConcurrentHashMap<>();

    public String create(User user) {
        String token = UUID.randomUUID().toString();
        tokenToUser.put(token, user);
        return token;
    }

    public User get(String token) {
        if (token == null) return null;
        return tokenToUser.get(token);
    }

    public void remove(String token) {
        if (token != null) tokenToUser.remove(token);
    }
}