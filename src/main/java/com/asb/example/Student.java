package com.asb.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Student {
	private String registrationNumber;
	private String name;
	private String grade;
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String correlationId =  UUID.randomUUID().toString();;
}