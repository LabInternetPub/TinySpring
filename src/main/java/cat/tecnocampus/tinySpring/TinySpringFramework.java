package cat.tecnocampus.tinySpring;

import cat.tecnocampus.tinySpring.core.ComponentFactory;
import cat.tecnocampus.tinySpring.core.ComponentScan;
import cat.tecnocampus.tinySpring.validationAOP.ValidationAOP;
import cat.tecnocampus.tinySpring.mvc.DispatcherServlet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TinySpringFramework {
    private static ComponentFactory componentFactory;
    private static ComponentScan componentScan;

    public static DispatcherServlet run(Class<?> clazz, String[] args) {
        Set<Class<?>> componentClasses  = new HashSet<>();;
        // Scans base package looking for component classes
        try {
            //Application components
            componentClasses.addAll(ComponentScan.componentScan(clazz.getPackageName() + ".application"));
            // Framework MVC infrastructure components (including built-in filters)
            componentClasses.addAll(
                    ComponentScan.componentScan("cat.tecnocampus.tinySpring.mvc")
            );
        } catch (IOException e) {
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
        return new DispatcherServlet(componentFactory.getContextContainer());
    }
}
