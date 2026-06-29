package cat.tecnocampus.tinySpring.mvc;

/**
 * A filter can inspect or short-circuit a request before it
 * reaches the controller. Filters are chained: each one
 * decides whether to pass control forward (chain.next())
 * or return a response immediately.
 */
public interface Filter {

    /**
     * Decides whether this filter applies to the given request.
     * Allows filters to be selective (e.g. only POST requests,
     * only /admin/** paths).
     */
    boolean supports(HttpRequest request);

    /**
     * Perform the filter logic.
     * Call chain.next(request) to pass control to the next filter
     * (or ultimately to the controller).
     */
    HttpResponse doFilter(HttpRequest request, FilterChain chain);
}