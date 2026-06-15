package com.runmvp.worker.sessionfinalizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SessionFinalizerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SessionFinalizerApplication.class, args);
    }
}
