package com.sameperson.porobot2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Porobot2Application {

    public static void main(String[] args) {
        SpringApplication.run(Porobot2Application.class, args);
    }

}
