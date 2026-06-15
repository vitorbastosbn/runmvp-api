package com.runmvp.worker.dataretention;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DataRetentionApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataRetentionApplication.class, args);
    }
}
