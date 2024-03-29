package com.example.rtdisservice;

import com.google.gson.JsonObject;
import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.realtime.Channel.MessageListener;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.Message;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


@Component
public class RTDISProducer {
    private final Logger logger = LoggerFactory.getLogger(RTDISProducer.class);

    @Bean
    public AblyRealtime ablyRealtime() {
        String ablyApiKey = "Hbqg4w.Al6JKQ:uWcSGybODbPtuAf4n6FXo456chiqQ0W9I5cqPFAVpr4";
        try {
            return new AblyRealtime(ablyApiKey);
        } catch (AblyException e) {

            e.printStackTrace();
            return null;
        }
    }

    @Bean
    public List<Channel> ablyChannels(AblyRealtime ablyRealtime) {
        List<String> channelNames = Arrays.asList(
                "[product:ably-tfl/tube]tube:940GZZLUNOW:arrivals",
                "[product:ably-tfl/tube]tube:940GZZLUCPK:arrivals",
//                "[product:ably-tfl/tube]tube:northern:940GZZLUSKW:arrivals",
                "[product:ably-tfl/tube]tube:940GZZLUWRR:arrivals"
//                "[product:ably-tfl/tube]tube:district:940GZZLUEHM:arrivals",
//                "[product:ably-tfl/tube]tube:northern:940GZZLUEUS:arrivals"
        );

        List<Channel> channels = new ArrayList<>();
        for (String channelName : channelNames) {
            channels.add(ablyRealtime.channels.get(channelName));
        }

        return channels;
    }


    @Bean
    public Producer<String, String> kafkaProducer() {
        String bootstrapServers = "localhost:9092";

        Properties kafkaProps = new Properties();
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return new KafkaProducer<>(kafkaProps);
    }

    @Bean
    public MessageListener ablyMessageListener(Producer<String, String> kafkaProducer) {
        return new MessageListener() {
            @Override
            public void onMessage(Message message) {
                JsonObject messageData = (JsonObject) message.data;

              
                kafkaProducer.send(new ProducerRecord<>("london-bus-SA", messageData.toString()));

                System.out.println("Received message: " + messageData.toString());
                logger.info("Received at RTDIS " + message.data.toString());
            }
        };
    }



    @Bean
    public ChannelMessageEvent channelMessageEvent(List<Channel> channels, MessageListener messageListener) {
        return new ChannelMessageEvent(channels, messageListener);
    }


    @EventListener
    public void configureAblyListener(ApplicationReadyEvent event) throws AblyException {
        List<Channel> channels = ablyChannels(ablyRealtime());
        MessageListener messageListener = ablyMessageListener(kafkaProducer());

        
        for (Channel channel : channels) {
            channel.subscribe(messageListener);
            System.out.println("Subscribed to channel: " + channel.name);
            logger.info("subscribed to channel %s", channel.name);
        }

        System.out.println("Ably listener configured for " + channels.size() + " channel(s)");
        logger.info("Ably listner configured for %s", channels.size());
    }


    static class ChannelMessageEvent {
        private final List<Channel> channels;
        private final MessageListener messageListener;

        public ChannelMessageEvent(List<Channel> channels, MessageListener messageListener) {
            this.channels = channels;
            this.messageListener = messageListener;
        }

        public List<Channel> getChannels() {
            return channels;
        }

        public MessageListener getMessageListener() {
            return messageListener;
        }
    }

}