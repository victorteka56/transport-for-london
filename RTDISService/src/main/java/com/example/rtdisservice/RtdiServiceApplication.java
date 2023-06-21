package com.example.rtdisservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
public class RtdiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RtdiServiceApplication.class, args);
    }
}
