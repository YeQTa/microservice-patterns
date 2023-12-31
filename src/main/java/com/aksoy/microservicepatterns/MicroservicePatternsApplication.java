package com.aksoy.microservicepatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class MicroservicePatternsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroservicePatternsApplication.class, args);
    }

}
