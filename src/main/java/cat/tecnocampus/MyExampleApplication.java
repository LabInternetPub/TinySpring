
package cat.tecnocampus;

import cat.tecnocampus.tinySpring.TinySpringFramework;
import cat.tecnocampus.tinySpring.webModule.HttpRequest;
import cat.tecnocampus.tinySpring.webModule.HttpResponse;
import cat.tecnocampus.tinySpring.webModule.SimpleWebFramework;

public class MyExampleApplication {
    public static void main(String[] args) {
        SimpleWebFramework app = TinySpringFramework.run(MyExampleApplication.class, args);

        // With a real web server, these calls would be performed from a REST client
        System.out.println();
        HttpRequest request = new HttpRequest("GET", "/hello/how are you");
        HttpResponse response = app.handleRequest(request);
        System.out.println(String.format("Response: status-> %s, body-> %s", response.getStatusCode(), response.getBody()));

        System.out.println();
        request = new HttpRequest("GET", "/hello/TinySpring");
        response = app.handleRequest(request);
        System.out.println(String.format("Response: status-> %s, body-> %s", response.getStatusCode(), response.getBody()));
    }
}
