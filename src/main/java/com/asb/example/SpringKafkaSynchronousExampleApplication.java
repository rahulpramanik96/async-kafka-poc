
package com.asb.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

@SpringBootApplication
public class SpringKafkaSynchronousExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringKafkaSynchronousExampleApplication.class, args);
	}
}