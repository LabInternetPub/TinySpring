package cat.tecnocampus.tinySpring.core;

import cat.tecnocampus.tinySpring.core.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class ComponentScan {
    private final static Logger logger = LoggerFactory.getLogger(ComponentScan.class);

    public static Set<Class<?>> componentScan(String basePackage) throws ClassNotFoundException, IOException {
        Set<Class<?>> components = new HashSet<>();

        List<File> classes = getFilesOfClassesInBasePackage(basePackage);
        classes.stream()
                .map(f -> getClassFromFile(basePackage, f))
                .filter(f -> isComponent(f))
                .forEach(c -> components.add(c));
        logComponentClasses(components);
        return components;
    }

    private static boolean isComponent(Class<?> clazz) {
        if (clazz.equals(Retention.class) || clazz.equals(Documented.class) || clazz.equals(Target.class)) { // to avoid infinite loop since Retention <--> Documented
            return false;
        }
        if (clazz.isAnnotationPresent(Component.class)) {
            return true;
        }
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            if (isComponent(annotation.annotationType())) return true;
        }
        return false;
    }

    private static List<File> getFilesOfClassesInBasePackage(String basePackage) {
        String path = basePackage.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);

        if (resource == null) {
            throw new IllegalArgumentException("No resource for " + path);
        }
        try {
            // Convert URL to URI first to properly handle encoding (spaces, special chars)
            URI uri = resource.toURI();
            File directory = new File(uri);

            if (!directory.exists()) {
                throw new IllegalArgumentException("Directory does not exist: " + directory);
            }
            File[] files = directory.listFiles();
            if (files == null) {
                return Collections.emptyList();
            }
            return Arrays.stream(files)
                    .filter(f -> f.getName().endsWith(".class"))
                    .toList();

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI for resource: " + resource, e);
        }
    }

    private static Class<?> getClassFromFile(String basePackage, File file) {
        String className = basePackage + '.' + file.getName().substring(0, file.getName().length() - 6);
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void logComponentClasses(Set<Class<?>> componentClasses) {
        componentClasses.forEach(c -> logger.info("Discovered Component class: {}", c.getName()));
    }
}