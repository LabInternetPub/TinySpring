package cat.tecnocampus.tinySpring.mvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Fake HTTP response class
public class HttpResponse {
    private final int statusCode;
    private final Map<String, Object> body;

    public HttpResponse() {
        this.statusCode = 200; // default status code
        this.body = new HashMap<>();
    }

    public HttpResponse(int statusCode, Map<String, Object> body) {
        this.statusCode = statusCode;
        this.body = Collections.unmodifiableMap(body);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, Object> getBody() {
        return body;
    }
}
