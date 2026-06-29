package cat.tecnocampus.tinySpring.mvc;

import cat.tecnocampus.tinySpring.core.annotation.Component;

@Component
public class LoggingFilter implements Filter {
    @Override
    public boolean supports(HttpRequest request) {
        return true; // applies to every request
    }

    @Override
    public HttpResponse doFilter(HttpRequest request, FilterChain chain) {
        System.out.println("--> {" + request.getMethod() + "} {" + request.getPath() + "}");
        HttpResponse response = chain.next(request); // delegate forward
        System.out.println("<-- {" + request.getMethod() + "} {" + response.getStatusCode() + "}");
        return response;
    }
}