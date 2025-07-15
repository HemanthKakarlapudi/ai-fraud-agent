package com.example.fraud.service;

import com.example.fraud.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class FraudDetectClientService {
    private static final Logger logger = LoggerFactory.getLogger(FraudDetectClientService.class);


    @Autowired
    RestTemplate restTemplate;


    @Autowired
    VectorStore vectorStore;


    private final ObjectMapper mapper = new ObjectMapper();


    private static final String ENDPOINT = "https://aihemanthhub1895302180.openai.azure.com/openai/deployments/gpt-4o/chat/completions?api-version=2024-12-01-preview";
    private static final String API_KEY = "BLIW7wHIjo4vNQ5HbQda5bJfWWq5Tgir8ZgWpMfEQsu2kXUwlaf6JQQJ99BGAC5RqLJXJ3w3AAAAACOGTtM5";

    private static final String MCP_SERVER_URL = "http://localhost:8090/api/server/fraud/transactions"; // Update if needed


    public ResponseEntity<String> detectFraudFromMcpServer() {
        try {
            String mcpServerUrl = "http://localhost:8090/api/server/fraud/transactions"; // Update if needed
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Transaction[]> response = restTemplate.getForEntity(mcpServerUrl, Transaction[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Transaction> transactions = Arrays.asList(response.getBody());
                return getFraudTransactions(transactions); // Reuse your existing method
            } else {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Failed to fetch transactions from MCP server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }

    public ResponseEntity<String> getFraudTransactions(List<Transaction> transactions) {

        try {

            StringBuilder csvSampleBuilder = new StringBuilder();
            csvSampleBuilder.append("customer_id,account_id,transaction_amount,source_account,transaction_date,is_fraud\n");

            int count = 0;
            for (Transaction tx : transactions) {
                if (count++ >= 10) break; // Limit to first 10 for prompt
                csvSampleBuilder.append(String.format("%s,%s,%.2f,%s,%s,%d\n",
                        tx.getCustomerId(),
                        tx.getAccountId(),
                        tx.getTransactionAmount(),
                        tx.getSourceAccount(),
                        tx.getTransactionDate(),
                        tx.getIsFraud()));
            }


            String promptTemplate = loadPromptTemplate("classpath:/prompts/prompts.txt");
            Map<String, Object> requestBody = getStringObjectMap(promptTemplate, csvSampleBuilder);


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", API_KEY);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(ENDPOINT, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null && message.containsKey("content")) {
                        return ResponseEntity.ok((String) message.get("content"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No response from model.");
    }

    private static Map<String, Object> getStringObjectMap(String promptTemplate, StringBuilder csvSampleBuilder) {
        String prompt = promptTemplate.replace("{{transactions}}", csvSampleBuilder.toString());


        // Prepare request
        Map<String, Object> systemMessage = Map.of("role", "system", "content", "You are a helpful fraud detection assistant.");
        Map<String, Object> userMessage = Map.of("role", "user", "content", prompt);

        Map<String, Object> requestBody = Map.of(
                "messages", List.of(systemMessage, userMessage),
                "max_tokens", 4096,
                "temperature", 1.0,
                "top_p", 1.0
        );
        return requestBody;
    }


    public String loadPromptTemplate(String filePath) throws IOException {
         Resource resource = new DefaultResourceLoader().getResource(filePath);
         return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

}