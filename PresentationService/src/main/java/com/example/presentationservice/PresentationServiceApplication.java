package com.example.presentationservice;

import com.example.presentationservice.message.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.types.AblyException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;

@SpringBootApplication
@EnableScheduling
public class PresentationServiceApplication implements CommandLineRunner {
    AblyRealtime ably;
    public PresentationServiceApplication() throws AblyException {
        ably = new AblyRealtime("qej_5A.CGaOkg:5zJYSv8GTuzqHemS-6S_nwWoKRCUnjWlz8yKkNF94mA");

    }


    public static void main(String[] args) {
        SpringApplication.run(PresentationServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        this.run();

    }

    @Scheduled(fixedDelay = 10000)
    public void run() throws AblyException {
        List<String> topics = List.of("TimeToStation", "LineNames", "Directions");
        Random random = new Random();
        List<Message> streamData = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            streamData.add(new Message(topics.get(random.nextInt(topics.size())), random.nextInt(5,20)));
        }

//
//
//       Map<String, List<Message>> sendData = new HashMap<>();
//        sendData.put("messages", streamData);
////        sendData.put("Direction", directionsData);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData;
        try {
            jsonData = objectMapper.writeValueAsString(streamData);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to convert to JSON: " + e.getMessage());
            return;
        }


        Channel channel = ably.channels.get("channel1");
        channel.publish("greeting", jsonData);
        System.out.println("Published message");

    }
}
