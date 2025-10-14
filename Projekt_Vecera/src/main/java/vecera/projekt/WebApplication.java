package vecera.projekt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // skenuje "vecera.projekt"
public class WebApplication {
    public static void main(String[] args) {

        // spustíme aplikaci
        SpringApplication.run(WebApplication.class, args);
    }
}