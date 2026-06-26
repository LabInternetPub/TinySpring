package cat.tecnocampus.tinySpring;

import cat.tecnocampus.tinySpring.core.ComponentFactory;
import cat.tecnocampus.tinySpring.core.ComponentScan;
import cat.tecnocampus.tinySpring.validationAOP.ValidationAOP;
import cat.tecnocampus.tinySpring.webModule.SimpleWebFramework;

import java.io.IOException;
import java.util.Set;

public class TinySpringFramework {
    private static ComponentFactory componentFactory;
    private static ComponentScan componentScan;

    public static SimpleWebFramework run(Class<?> clazz, String[] args) {
        Set<Class<?>> componentClasses;
        // Scans base package looking for component classes
        try {
            componentClasses = ComponentScan.componentScan(clazz.getPackageName() + ".application");
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }

        //Creates the application context
        componentFactory = new ComponentFactory();

        // Scans and instantiates the components
        componentFactory.instantiateComponents(componentClasses);

        // Creates proxies for the components that need validation
        ValidationAOP.createAndRegisterValidationProxies(componentFactory.getContextContainer());

        // Injects the dependencies
        componentFactory.injectDependencies();

        // Creates the web server or framework
        SimpleWebFramework app = new SimpleWebFramework(componentFactory.getContextContainer());

        return app;
    }

}
