package com.nds.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class SmsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmsServiceApplication.class, args);
    }
}
