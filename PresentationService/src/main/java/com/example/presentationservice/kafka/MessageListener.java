package com.example.presentationservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.opencsv.CSVWriter;

@Component
public class MessageListener {

    @KafkaListener(topics = "send-to-csv")
    public void listen(String message) {
        String filePath = System.getProperty("user.home") + "/Desktop/finalproject-csv/data.csv";
        // Check if the file is empty
        boolean fileIsEmpty = !new File(filePath).exists() || new File(filePath).length() == 0;


        // Append data to CSV file
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath, true))) {
            if (fileIsEmpty) {
                writer.writeNext(new String[]{"Topic", "Message", "Timestamp"});
            }
            writer.writeNext(new String[]{message});
            System.out.println("Data appended to CSV file successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
