package Sakari;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SakariApplication {

    public static void main(String[] args) {
        SpringApplication.run(SakariApplication.class, args);
    }
}
