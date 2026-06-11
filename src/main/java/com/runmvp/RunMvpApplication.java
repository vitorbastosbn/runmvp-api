package com.runmvp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RunMvpApplication {
    public static void main(String[] args) {
        SpringApplication.run(RunMvpApplication.class, args);
    }
}
