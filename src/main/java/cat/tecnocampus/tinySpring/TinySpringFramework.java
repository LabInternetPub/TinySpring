package cat.tecnocampus.tinySpring;

import cat.tecnocampus.tinySpring.core.ComponentFactory;
import cat.tecnocampus.tinySpring.validationAOP.ValidationAOP;
import cat.tecnocampus.tinySpring.webModule.SimpleWebFramework;

public class TinySpringFramework {
    private static ComponentFactory componentFactory;

    public static SimpleWebFramework run(Class<?> clazz, String[] args) {
        //Creates the application context
        componentFactory = new ComponentFactory(clazz.getPackageName() + ".application");

        // Scans and instantiates the components
        componentFactory.scanAndInstantiateComponents();

        // Creates proxies for the components that need validation
        ValidationAOP.createAndRegisterValidationProxies(componentFactory.getContextContainer());

        // Injects the dependencies
        componentFactory.injectDependencies();

        // Creates the web server or framework
        SimpleWebFramework app = new SimpleWebFramework(componentFactory.getContextContainer());

        return app;
    }

}
