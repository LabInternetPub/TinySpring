package cat.tecnocampus.tinySpring.mvc;

import java.lang.reflect.Method;

/**
 * Holds everything needed to invoke a controller method.
 * Built once at startup; reused on every matching request.
 */
public class HandlerMethod {
    private final Object controller;
    private final Method method;

    public HandlerMethod(Object controller, Method method) {
        this.controller = controller;
        this.method = method;
    }

    public HttpResponse invoke(HttpRequest request) {
        try {
            return (HttpResponse) method.invoke(controller, request);
        } catch (Exception e) {
            throw new RuntimeException("Error invoking handler " + method.getName(), e);
        }
    }

    @Override
    public String toString() {
        return controller.getClass().getSimpleName() + "#" + method.getName();
    }
}