package cat.tecnocampus.tinySpring.mvc;

import cat.tecnocampus.tinySpring.core.ApplicationContextContainer;
import cat.tecnocampus.tinySpring.core.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Miniature DispatcherServlet.
 *
 * At construction time it:
 *   1. scans all @RestController beans
 *   2. builds a route table (RouteKey -> HandlerMethod)
 *   3. collects all Filter beans from the container
 *
 * At request time it:
 *   1. resolves the HandlerMethod from the route table
 *   2. builds a FilterChain for this request
 *   3. lets the chain run (filters -> handler)
 *   4. returns a proper HttpResponse (never null)
 */
public class DispatcherServlet {
    private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);

    private final Map<RouteKey, HandlerMethod> routeTable = new LinkedHashMap<>();
    private final List<Filter> filters = new ArrayList<>();

    public DispatcherServlet(ApplicationContextContainer context) {
        buildRouteTable(context);
        collectFilters(context);
    }

    // ---------------------------------------------------------------
    // Startup
    // ---------------------------------------------------------------

    private void buildRouteTable(ApplicationContextContainer context) {
        context.getComponents().stream()
                .filter(bean -> bean.getClass().isAnnotationPresent(RestController.class))
                .forEach(this::registerController);

        logger.info("Route table built with {} routes", routeTable.size());
        routeTable.forEach((key, handler) ->
                logger.info("  {} -> {}", key, handler));
    }

    private void registerController(Object controller) {
        // Support @RequestMapping on the class as a base path
        String basePath = "";
        if (controller.getClass().isAnnotationPresent(RequestMapping.class)) {
            basePath = controller.getClass().getAnnotation(RequestMapping.class).value();
        }

        for (Method method : controller.getClass().getMethods()) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping mapping = method.getAnnotation(RequestMapping.class);
                RouteKey key = new RouteKey(mapping.method(), basePath + mapping.value());
                routeTable.put(key, new HandlerMethod(controller, method));
            }
        }
    }

    private void collectFilters(ApplicationContextContainer context) {
        context.getComponents().stream()
                .filter(bean -> bean instanceof Filter)
                .map(bean -> (Filter) bean)
                .forEach(filters::add);

        logger.info("Registered {} filters", filters.size());
    }

    // ---------------------------------------------------------------
    // Request handling
    // ---------------------------------------------------------------

    public HttpResponse handleRequest(HttpRequest request) {
        HandlerMethod handler = resolveHandler(request);

        if (handler == null) {
            return notFound(request);
        }

        FilterChain chain = new FilterChain(new ArrayList<>(filters), handler);
        try {
            return chain.next(request);
        } catch (Exception e) {
            logger.error("Unhandled exception for {} {}", request.getMethod(), request.getPath(), e);
            return internalError(e);
        }
    }

    private HandlerMethod resolveHandler(HttpRequest request) {
        HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());

        RouteKey exact = new RouteKey(method, request.getPath());
        if (routeTable.containsKey(exact)) {
            return routeTable.get(exact);
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Default error responses
    // ---------------------------------------------------------------

    private HttpResponse notFound(HttpRequest request) {
        logger.warn("No handler found for {} {}", request.getMethod(), request.getPath());
        return new HttpResponse(404, Map.of(
                "error", "Not Found",
                "path", request.getPath()
        ));
    }

    private HttpResponse internalError(Exception e) {
        return new HttpResponse(500, Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage() != null ? e.getMessage() : "Unknown error"
        ));
    }
}