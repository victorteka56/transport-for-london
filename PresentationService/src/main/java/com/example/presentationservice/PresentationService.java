package com.example.presentationservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

@Component
public class PresentationService {
    private static final String OUTPUT_FILE_PATH = "data.csv";
    private static final String[] DATA_TOPICS = {"DS_LineName", "DS_Direction", "DS_DestinationName", "DS_CurrentLocation", "DS_Towards"};
    private static final int MAX_RECORDS = 10;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private CSVPrinter csvPrinter;

    @EventListener
    public void startPresentationService(ApplicationReadyEvent event) {
        KafkaConsumer<String, String> kafkaConsumer = createKafkaConsumer();

        try (FileWriter writer = new FileWriter(OUTPUT_FILE_PATH, true)) {
            csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

            for (String topic : DATA_TOPICS) {
                subscribeToTopic(kafkaConsumer, topic);

                int processedRecords = 0;
                while (processedRecords < MAX_RECORDS) {
                    ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record : consumerRecords) {
                        String value = record.value();

                        // Extract Timestamp and Message values from the JSON
                        String timestamp = extractTimestamp(value);
                        String message = extractMessage(value);

                        // Write the values to the CSV file
                        writeValues(timestamp, message);

                        processedRecords++;
                        if (processedRecords >= MAX_RECORDS) {
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to create or write to the output file: " + e.getMessage());
        } finally {
            kafkaConsumer.close();
        }
    }

    private KafkaConsumer<String, String> createKafkaConsumer() {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "presentation-group");

        return new KafkaConsumer<>(consumerProps);
    }

    private void subscribeToTopic(KafkaConsumer<String, String> kafkaConsumer, String topic) {
        kafkaConsumer.subscribe(Collections.singletonList(topic));
    }

    private String extractTimestamp(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode timestampNode = rootNode.get("Timestamp");
            if (timestampNode != null && !timestampNode.isNull()) {
                return timestampNode.asText();
            }
        } catch (IOException e) {
            System.err.println("Failed to extract Timestamp from JSON: " + e.getMessage());
        }
        return "";
    }

    private String extractMessage(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode messageNode = rootNode.get("Message");
            if (messageNode != null && !messageNode.isNull()) {
                return messageNode.asText();
            }
        } catch (IOException e) {
            System.err.println("Failed to extract Message from JSON: " + e.getMessage());
        }
        return "";
    }

    private void writeValues(String timestamp, String message) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        Row row = sheet.createRow(0);
        Cell timestampCell = row.createCell(0);
        Cell messageCell = row.createCell(1);

        timestampCell.setCellValue(timestamp);
        messageCell.setCellValue(message);

        try (FileOutputStream outputStream = new FileOutputStream("data.xlsx")) {
            workbook.write(outputStream);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(PresentationService.class, args);
    }
}
