package com.example.fraud;

import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
//@EnableCosmosRepositories(basePackages = "com.example.springaiapp.repository")
public class AIFraudAgebtAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AIFraudAgebtAppApplication.class, args);
	}

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

}
