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
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ComponentScan {
    private static final Logger logger = LoggerFactory.getLogger(ComponentScan.class);

    public static Set<Class<?>> componentScan(String basePackage) throws IOException {
        Set<Class<?>> components = new HashSet<>();

        List<String> classNames = getClassNamesInBasePackage(basePackage);
        classNames.stream()
                .map(ComponentScan::loadClass)
                .filter(Objects::nonNull)
                .filter(ComponentScan::isComponent)
                .forEach(components::add);

        logComponentClasses(components);
        return components;
    }

    private static boolean isComponent(Class<?> clazz) {
        if (clazz.equals(Retention.class) || clazz.equals(Documented.class) || clazz.equals(Target.class)) {
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

    private static List<String> getClassNamesInBasePackage(String basePackage) throws IOException {
        String path = basePackage.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        List<String> classNames = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();

            if ("file".equals(protocol)) {
                classNames.addAll(getClassNamesFromFileSystem(basePackage, resource));
            } else if ("jar".equals(protocol)) {
                classNames.addAll(getClassNamesFromJar(basePackage, path, resource));
            } else {
                logger.warn("Unsupported resource protocol {} for {}", protocol, resource);
            }
        }

        return classNames;
    }

    private static List<String> getClassNamesFromFileSystem(String basePackage, URL resource) {
        try {
            File directory = new File(resource.toURI());
            if (!directory.exists()) {
                return Collections.emptyList();
            }

            File[] files = directory.listFiles();
            if (files == null) {
                return Collections.emptyList();
            }

            List<String> classNames = new ArrayList<>();
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    String simpleName = file.getName().substring(0, file.getName().length() - 6);
                    classNames.add(basePackage + "." + simpleName);
                }
            }
            return classNames;

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI for resource: " + resource, e);
        }
    }

    private static List<String> getClassNamesFromJar(String basePackage, String path, URL resource) throws IOException {
        JarURLConnection connection = (JarURLConnection) resource.openConnection();
        JarFile jarFile = connection.getJarFile();

        List<String> classNames = new ArrayList<>();
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();

            if (entryName.startsWith(path + "/")
                    && !entry.isDirectory()
                    && entryName.endsWith(".class")) {

                String className = entryName
                        .replace('/', '.')
                        .substring(0, entryName.length() - 6);

                // only immediate children of the package, not subpackages
                String remaining = className.substring(basePackage.length() + 1);
                if (!remaining.contains(".")) {
                    classNames.add(className);
                }
            }
        }

        return classNames;
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            logger.error("Could not load class {}", className, e);
            return null;
        }
    }

    private static void logComponentClasses(Set<Class<?>> componentClasses) {
        componentClasses.forEach(c -> logger.info("Discovered Component class: {}", c.getName()));
    }
}