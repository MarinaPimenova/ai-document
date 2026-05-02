package com.training.app;

import org.springframework.boot.SpringApplication;

public class TestAiDocumentApplication {

    public static void main(String[] args) {
        SpringApplication.from(AIDocumentApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
