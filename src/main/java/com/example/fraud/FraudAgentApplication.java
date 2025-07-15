package com.example.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
//@EnableCosmosRepositories(basePackages = "com.example.springaiapp.repository")
public class FraudAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(FraudAgentApplication.class, args);
	}

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

}
