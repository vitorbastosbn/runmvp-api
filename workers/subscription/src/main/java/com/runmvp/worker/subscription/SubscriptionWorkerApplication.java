package com.runmvp.worker.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SubscriptionWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubscriptionWorkerApplication.class, args);
    }
}
