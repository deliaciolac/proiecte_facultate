package dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {
    private final Action action;
    private String token;
    private final Map<String, Object> data = new HashMap<>();

    public Request(Action action) { this.action = action; }

    public Action getAction() { return action; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Map<String, Object> getData() { return data; }

    public Request put(String key, Object value) {
        data.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> cls) {
        return (T) data.get(key);
    }
}