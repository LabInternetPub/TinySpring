package cat.tecnocampus.tinySpring.mvc;

public record RouteKey(HttpMethod method, String path) {
    @Override
    public String toString() {
        return method + " " + path;
    }
}
