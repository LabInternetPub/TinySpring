package cat.tecnocampus.tinySpring.core;

import cat.tecnocampus.tinySpring.webProject.HttpRequest;
import cat.tecnocampus.tinySpring.webProject.HttpResponse;
import cat.tecnocampus.tinySpring.webProject.LiteSpringBoot;

public class TinySpringApplication {
    private static ComponentFactory componentFactory;

    public static void run(Class<?> clazz, String[] args) {
        componentFactory = new ComponentFactory(clazz.getPackageName() + ".application");
        componentFactory.buildContext();

        LiteSpringBoot app = new LiteSpringBoot(componentFactory);
        HttpRequest request = new HttpRequest("GET", "/hello");
        HttpResponse response = app.handleRequest(request);
        System.out.println(String.format("Response: status-> %s, body-> %s", response.getStatusCode(), response.getBody()));
    }
}
