package cat.tecnocampus.tinySpring.mvc;

import cat.tecnocampus.tinySpring.core.annotation.Component;

import java.util.Map;

@Component
public class AuthFilter implements Filter {

    @Override
    public boolean supports(HttpRequest request) {
        return request.getPath().contains("/admin");
    }

    @Override
    public HttpResponse doFilter(HttpRequest request, FilterChain chain) {
        String token = request.getPath();
        if (!token.contains("authorized")) {
            Map<String, Object> responseBody = Map.of("message", "Security filter", "result", "Unauthorized");
            return new HttpResponse(401, responseBody);
        }
        return chain.next(request);
    }
}