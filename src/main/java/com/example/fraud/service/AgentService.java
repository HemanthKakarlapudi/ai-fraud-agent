package com.example.fraud.service;

import com.example.fraud.model.CustomerNotification;
import com.example.fraud.model.EmailContent;
import com.example.fraud.model.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class AgentService {
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);


    @Autowired
    RestTemplate restTemplate;

    private static final String MCP_CLIENT_URL = "http://localhost:8090/api/client/findFraudTransactions"; // Update if needed

    private static final String MCP_EMAIL_CLIENT_URL = "http://localhost:8090/api/client/sendEmailNotification"; // Update if needed


    public ResponseEntity<String> fraudClientServiceTransactions(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(prompt, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(MCP_CLIENT_URL, request, String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred: " + e.getMessage());
        }
    }

    public ResponseEntity<String> emailClientNotification(EmailContent emailContent) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectMapper objectMapper  = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(emailContent);
            HttpEntity request = new HttpEntity<>(jsonBody,headers);

            ResponseEntity<String> response = restTemplate.postForEntity(MCP_EMAIL_CLIENT_URL, request, String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred: " + e.getMessage());
        }
    }

    public ResponseEntity<String> sendNotification(String responseEntity) throws JsonProcessingException {
//        String jsonResponse = String.valueOf(responseEntity);


        int start = responseEntity.indexOf('[');
        int end = responseEntity.lastIndexOf(']') + 1;
        String json = responseEntity.substring(start, end);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        List<CustomerNotification> customerObject = new ArrayList<>();

        for (JsonNode node : root) {
            CustomerNotification summary = new CustomerNotification();
            summary.setCustomerId(node.get("customerId").asText());
            summary.setFraudReason(node.get("reason").asText());

            EmailContent emailContent = new EmailContent();
            emailContent.setRecipientId("hemanth.kakarlapudi@capgemini.com");
            emailContent.setSubject("Test Email from Postman");
            emailContent.setBody("This is a test email sent using Azure Communication Services from Spring Boot");
            summary.setEmailContent(emailContent);
            System.out.println(summary.getCustomerId());
            System.out.println(summary.getFraudReason());
            customerObject.add(summary);
        }
        System.out.println("Size-->"+customerObject.size());
        return ResponseEntity.ok(customerObject.toString());
    }

}