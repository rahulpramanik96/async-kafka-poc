package com.asb.example;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaController {

	@Value("${kafka.request.topic}")
	private String requestTopic;

	@Autowired
	private KafkaTemplate<String, Student> kafkaTemplate;

	@Autowired
	private ResultListener resultListener;

	@PostMapping("/get-result")
	public ResponseEntity<Result> getResult(@RequestBody Student student)
			throws InterruptedException, ExecutionException {

		// Generate a correlation ID
		String correlationId = student.getCorrelationId();

		// Validate correlation ID
		if (correlationId == null || correlationId.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		// Register a CompletableFuture for this correlation ID
		CompletableFuture<Result> resultFuture = new CompletableFuture<>();
		resultListener.registerFuture(correlationId, resultFuture);

		// Create headers including the correlation ID
		// Create headers including the correlation ID
		Headers headers = new RecordHeaders()
				.add("kafka_correlationId", correlationId.getBytes());

		ProducerRecord<String, Student> record = new ProducerRecord<>(
				requestTopic,  // Topic
				null,          // Partition (null for automatic partitioning)
				correlationId, // Key
				student,       // Value
				headers        // Headers
		);
		kafkaTemplate.send(record);

		// Wait for the result and return it
		Result result = resultFuture.get();
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}
