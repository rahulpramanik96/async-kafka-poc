package com.asb.example;

import java.util.concurrent.ThreadLocalRandom;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DummyVaultListener {

    @Autowired
    private KafkaTemplate<String, Result> kafkaTemplate;

    @Value("${kafka.reply.topic}")
    private String replyTopic;


    @KafkaListener(topics = "${kafka.request.topic}", groupId = "${kafka.group.id}")
    public void processVaultLogic(Student student) {
        System.out.println("Processing in Vault...");

        // Simulate result calculation
        double total = ThreadLocalRandom.current().nextDouble(2.5, 9.9);
        Result result = new Result();
        result.setName(student.getName());
        result.setResult((total > 3.5) ? "Pass" : "Fail");
        result.setPercentage(String.valueOf(total * 10).substring(0, 4) + "%");

        // Send the result to the result topic
        String correlationId = student.getCorrelationId(); // Assuming Student object has this method
        Headers headers = new RecordHeaders()
                .add("kafka_correlationId", correlationId.getBytes());

        // Create a ProducerRecord with headers
        ProducerRecord<String, Result> record = new ProducerRecord<>(
                replyTopic,  // Topic
                null,        // Partition (null for automatic partitioning)
                correlationId, // Key
                result,      // Value
                headers      // Headers
        );
        kafkaTemplate.send(record);
        System.out.println("Wrote sucessfully in Response Topic ");
    }
}
