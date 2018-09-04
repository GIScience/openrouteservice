package heigit.ors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@ServletComponentScan("heigit.ors.servlet.listeners")
@SpringBootApplication
public class Application extends SpringBootServletInitializer {
    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }
}
