package com.asb.example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ResultListener {

    private final ConcurrentHashMap<String, CompletableFuture<Result>> responseFutures = new ConcurrentHashMap<>();

    @KafkaListener(topics = "${kafka.reply.topic}", groupId = "${kafka.group.id}")
    public void receiveResult(ConsumerRecord<String, Result> consumerRecord) {
        System.out.println("Got result in response topic "+consumerRecord);
        String correlationId = consumerRecord.key();
        Result result = consumerRecord.value();
        Headers headers = consumerRecord.headers();

        // Extract the correlationId from headers if needed
        String correlationIdFromHeader = getCorrelationIdFromHeaders(headers);

        // Use the correlationId from the record's key if the header is not present
        if (correlationIdFromHeader == null) {
            correlationIdFromHeader = correlationId;
        }

        // Complete the CompletableFuture with the result
        CompletableFuture<Result> future = responseFutures.get(correlationIdFromHeader);
        if (future != null) {
            future.complete(result);
            responseFutures.remove(correlationIdFromHeader);
        } else {
            System.err.println("No CompletableFuture found for correlation ID: " + correlationIdFromHeader);
        }
    }

    public void registerFuture(String key, CompletableFuture<Result> future) {
        System.out.println("Registering future with key: " + key);
        if (key == null || future == null) {
            System.err.println("Key or future is null");
            return;
        }
        responseFutures.put(key, future);
    }

    private String getCorrelationIdFromHeaders(Headers headers) {
        Header correlationIdHeader = headers.lastHeader("kafka_correlationId");
        if (correlationIdHeader != null) {
            return new String(correlationIdHeader.value());
        }
        return null;
    }
}
