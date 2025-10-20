package cat.tecnocampus.tinySpring.validationAOP;

import java.lang.reflect.Proxy;

public class ValidationProxyFactory {
    public static Object createProxy(Object target) {
        return Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new ValidationHandler(target));
    }
}
