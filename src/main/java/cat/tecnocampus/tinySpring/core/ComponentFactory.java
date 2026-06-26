package cat.tecnocampus.tinySpring.core;

import cat.tecnocampus.tinySpring.core.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Set;


public class ComponentFactory {
    private final ApplicationContextContainer applicationContextContainer;
    private final Logger logger = LoggerFactory.getLogger(ComponentFactory.class);

    public ComponentFactory() {
        this.applicationContextContainer = new ApplicationContextContainer();
    }

    public ApplicationContextContainer getContextContainer() {
        return applicationContextContainer;
    }

    public void instantiateComponents(Set<Class<?>> componentClasses) {
        componentClasses.stream()
                .map(this::newComponentObject) //instantiates an object for each class
                .forEach(o -> applicationContextContainer.register(o.getClass(), o));  //adds the object to the application context
    }

    public void injectDependencies() {
        applicationContextContainer.getComponents().forEach(this::autowire);
    }

    private Object newComponentObject(Class<?> clazz) {
        Object componentObject = null;
        try {
            componentObject = clazz.getDeclaredConstructor().newInstance();
            logger.info("Instantiated component: {}", clazz.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return componentObject;
    }

    private void autowire(Object instance) {
        Field[] fields = instance.getClass().getDeclaredFields();
        Arrays.stream(fields)
                .filter(f -> f.isAnnotationPresent(Autowired.class))
                .forEach(f -> autowireField(instance, f));
    }

    private void autowireField(Object instance, Field field) {
        Object dependency = applicationContextContainer.getComponentOfType(field.getType());
        if (dependency != null) {
            field.setAccessible(true);
            try {
                field.set(instance, dependency);
                logAutowireInjection(instance.getClass(), dependency.getClass());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            field.setAccessible(false);
        }
    }

    private void logAutowireInjection(Class<?> instance, Class<?> dependency) {
        if (Proxy.isProxyClass(dependency)) {
            Class<?>[] interfaces = dependency.getInterfaces();
            if (interfaces.length > 0) {
                logger.info("Instance {} injected with proxy dependency {} implementing interface {}", instance.getName(), dependency.getName(), interfaces[0].getName());
            } else {
                logger.info("Instance {} injected with proxy dependency {}", instance.getName(), dependency.getName());
            }
        } else {
            logger.info("Instance {} injected with dependency {}", instance.getName(), dependency.getName());
        }
    }
}
