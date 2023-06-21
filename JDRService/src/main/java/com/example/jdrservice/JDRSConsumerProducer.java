package com.example.jdrservice;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.EnableKafka;
import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.types.AblyException;


import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@SpringBootApplication
@EnableKafka
public class JDRSConsumerProducer {
    private static final Set<String> existingTopics = new HashSet<>();


    @Bean
    public KafkaProducer<String, String> kafkaProducer() {
        Properties kafkaProps = new Properties();
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return new KafkaProducer<>(kafkaProps);
    }

    @Bean
    public MessageListener kafkaMessageListener(KafkaProducer<String, String> kafkaProducer) {
        return new MessageListener(kafkaProducer);
    }

    public static class MessageListener {
        private KafkaProducer<String, String> kafkaProducer;

        public MessageListener(KafkaProducer<String, String> kafkaProducer) {
            this.kafkaProducer = kafkaProducer;
        }

        public void onMessage(String topicName, String message) {
            System.out.println("Received message: " + message);

            JsonElement jsonElement = JsonParser.parseString(message);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            Set<String> fieldNames = jsonObject.keySet();
            for (String fieldName : fieldNames) {
                    JsonArray fieldArray = jsonObject.getAsJsonArray(fieldName);
                    if (fieldArray != null) {
                        for (JsonElement fieldElement : fieldArray) {
                            JsonObject fieldObject = fieldElement.getAsJsonObject();

                            for (String elementName : fieldObject.keySet()) {
                                if (elementName.equals("LineName") || elementName.equals("Direction") || elementName.equals("DestinationName") || elementName.equals("CurrentLocation") || elementName.equals("Towards")) {
                                    String topic = "DS_" + elementName;
                                    JsonElement elementValue = fieldObject.get(elementName);

                                    kafkaProducer.send(new ProducerRecord<>(topic, elementValue.toString()));
                                    sendToStream(elementName, elementValue.toString());
                                }
                            }
                        }

                }
            }
        }



        public void sendToStream(String key, String value) {
            kafkaProducer.send(new ProducerRecord<>("stream-topic", key, value));


            try {
                AblyRealtime ably = new AblyRealtime("qej_5A.CGaOkg:5zJYSv8GTuzqHemS-6S_nwWoKRCUnjWlz8yKkNF94mA");
                Channel channel = ably.channels.get("channel1");
                channel.publish(key, value);

                System.out.println("Published to Ably channel: [Key: " + key + ", Value: " + value + "]");
            } catch (AblyException e) {
                System.err.println("Failed to publish message to Ably channel: " + e.getMessage());
            }
        }

    }

    @EventListener
    public void configureKafkaListener(ApplicationReadyEvent event) {
        KafkaProducer<String, String> kafkaProducer = event.getApplicationContext().getBean(KafkaProducer.class);
        MessageListener messageListener = kafkaMessageListener(kafkaProducer);

        subscribeToKafkaTopics(messageListener);


        System.out.println("Kafka listener configured");
    }

    private void subscribeToKafkaTopics(MessageListener messageListener) {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group2");
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(consumerProps);

        kafkaConsumer.subscribe(Collections.singletonList("london-bus-SA"));


        while (true) {
            ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : consumerRecords) {
                String key = record.key();
                String value = record.value();


                System.out.println("Received message: " + value);

                messageListener.onMessage(key, value);
            }
        }
    }
}