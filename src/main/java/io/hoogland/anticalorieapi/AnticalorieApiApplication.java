package io.hoogland.anticalorieapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AnticalorieApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnticalorieApiApplication.class, args);
    }

}
