package com.jjap.berries;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BerriesApplication {

    public static void main(String[] args) {
        SpringApplication.run(BerriesApplication.class, args);
    }

}
