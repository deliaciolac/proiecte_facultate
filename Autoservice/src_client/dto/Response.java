package dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Response implements Serializable {
    private boolean ok;
    private String message;
    private String token;
    private final Map<String, Object> data = new HashMap<>();

    public static Response ok(String msg) {
        Response r = new Response();
        r.ok = true;
        r.message = msg;
        return r;
    }

    public static Response fail(String msg) {
        Response r = new Response();
        r.ok = false;
        r.message = msg;
        return r;
    }

    public boolean isOk() { return ok; }
    public String getMessage() { return message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Map<String, Object> getData() { return data; }

    public Response put(String key, Object value) {
        data.put(key, value);
        return this;
    }
}