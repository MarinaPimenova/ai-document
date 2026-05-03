package com.training.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestAiDocumentApplication {

    public static void main(String[] args) {
        SpringApplication.from(AIDocumentApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }

}