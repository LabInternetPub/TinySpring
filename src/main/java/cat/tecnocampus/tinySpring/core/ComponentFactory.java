package cat.tecnocampus.tinySpring.core;

import cat.tecnocampus.tinySpring.core.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;

public class ComponentFactory {
    private final ApplicationContainer applicationContainer;
    private final ComponentScan componentScan;

    public ComponentFactory(String packageName) {
        this.applicationContainer = new ApplicationContainer();
        this.componentScan = new ComponentScan(packageName);
    }

    public ApplicationContainer getContainer() {
        return applicationContainer;
    }

    public void buildContext() {
        Set<Class<?>> componentClasses = getComponentClasses();
        componentClasses.stream()
                .map(this::newComponentObject)
                .forEach(o -> applicationContainer.register(o.getClass(), o));

        applicationContainer.getComponents().forEach(this::autowire);
    }

    private Object newComponentObject(Class<?> clazz) {
        Object componentObject = null;
        try {
            componentObject = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return componentObject;
    }

    private Set<Class<?>> getComponentClasses() {
        Set<Class<?>> componentClasses;
        try {
            componentClasses = componentScan.componentScan();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return componentClasses;
    }

    private void autowire(Object instance) {
        Field[] fields = instance.getClass().getDeclaredFields();
        Arrays.stream(fields)
                .filter(f -> f.isAnnotationPresent(Autowired.class))
                .forEach(f -> autowireField(instance, f));
    }

    private void autowireField(Object instance, Field field) {
        Object dependency = applicationContainer.getComponentOfType(field.getType());
        if (dependency != null) {
            field.setAccessible(true);
            try {
                field.set(instance, dependency);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
