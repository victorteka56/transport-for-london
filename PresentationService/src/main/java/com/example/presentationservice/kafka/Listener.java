package com.example.presentationservice.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@Component
//@RequestMapping("/messages")
public class Listener {


    @KafkaListener(topics = {"DS_CurrentLocation", "DS_DestinationName", "DS_Towards"})
    public void listen(String message) {
        // Process the received message as needed
        System.out.println("Received message: " + message);
        // Send the message to the React app using REST
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(message, headers);

        String apiUrl = "http://localhost:3000";
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                request,
                String.class
        );

    }
}
