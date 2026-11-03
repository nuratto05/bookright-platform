package com.bookrights;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication  // Marks this as the main Spring Boot application
@EnableCaching 
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args); // Starts the Spring Boot app
    }
}
