package cat.tecnocampus.tinySpring.mvc;

// Fake HTTP request class
public class HttpRequest {
    String method;
    public String path;

    public HttpRequest(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}
