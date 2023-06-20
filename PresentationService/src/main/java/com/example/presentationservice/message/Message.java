package com.example.presentationservice.message;


import java.io.Serializable;

public class Message implements Serializable {
    private String topic;

    public Message(String topic, Integer message) {
        this.topic = topic;
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getMessage() {
        return message;
    }

    public void setMessage(Integer message) {
        this.message = message;
    }

    private Integer message;
    public Message() {
    }
}
