package com.nds.push;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class PushServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PushServiceApplication.class, args);
    }
}
