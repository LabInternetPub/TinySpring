package cat.tecnocampus.tinySpring.mvc;

import java.util.List;

/**
 * Chains a list of filters and a terminal handler together.
 * Each filter calls chain.next() to proceed; the last step
 * invokes the actual HandlerMethod.
 */
public class FilterChain {
    private final List<Filter> filters;
    private final HandlerMethod handler;
    private int index = 0;

    public FilterChain(List<Filter> filters, HandlerMethod handler) {
        this.filters = filters;
        this.handler = handler;
    }

    public HttpResponse next(HttpRequest request) {
        // Walk through filters that support this request
        while (index < filters.size()) {
            Filter current = filters.get(index++);
            if (current.supports(request)) {
                return current.doFilter(request, this);
            }
        }
        // All applicable filters passed — invoke the real handler
        return handler.invoke(request);
    }
}