package com.example.fraud.service;

import com.example.fraud.service.TransactionDataService;
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
public class FraudDetectServerService {
    private static final Logger logger = LoggerFactory.getLogger(FraudDetectServerService.class);



    @Autowired
    VectorStore vectorStore;

    @Autowired
    private TransactionDataService transactionService;


    private static final String ENDPOINT = "";
    private static final String API_KEY = "";


    public void loadTransactionData() throws IOException {

        try {
            Resource resource = new DefaultResourceLoader().getResource("classpath:/static/synthetic_transaction_data.csv");

            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));


            List<Document> documents = new ArrayList<>();
            String header = reader.readLine(); // skip header
            String line;
            int index = 0;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                System.out.println("✅ Transaction " + Arrays.toString(parts) + " transactions into the vector store.");

                if (parts.length >= 8) {
                    Transaction transaction = new Transaction();
                    transaction.setCustomerId(parts[0]);
                    transaction.setAccountId(parts[1]);
                    transaction.setTransactionDate(parts[2]);
                    transaction.setTransactionAmount(Double.parseDouble(parts[3]));
                    transaction.setCurrency(parts[4]);
                    transaction.setTransactionType(parts[5]);
                    transaction.setSourceAccount(parts[6]);
                    transaction.setIsFraud(Integer.parseInt(parts[7]));

                    String content = String.format(
                            "Customer %s made a %s of %.2f %s on %s from account %s to %s. Fraud status: %s.",
                            transaction.getCustomerId(), transaction.getTransactionType(), transaction.getTransactionAmount(),
                            transaction.getCurrency(), transaction.getTransactionDate(), transaction.getSourceAccount(),
                            transaction.getAccountId(), transaction.getIsFraud() == 1 ? "Yes" : "No"
                    );
                    Map<String, Object> metadata = Map.of(
                            "customer_id", transaction.getCustomerId(),
                            "account_id", transaction.getAccountId(),
                            "transaction_date", transaction.getTransactionDate(),
                            "is_fraud", transaction.getIsFraud()
                    );


                    documents.add(new Document(UUID.randomUUID().toString(), content, metadata));
                }
            }

            vectorStore.add(documents);
            System.out.println("✅ Loaded " + documents.size() + " transactions into the vector store.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Tool(description = "Find fraud transactions")
    public ResponseEntity<String> detectFraudFromCsvFromLocal() {
        try {
//                Resource resource = new DefaultResourceLoader().getResource("classpath:/static/dummy_transaction_data.csv");
            Resource resource = new DefaultResourceLoader().getResource("classpath:/static/synthetic_transaction_data.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            // Read header + first 10 rows
            List<String> lines = new ArrayList<>();
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count <= 10) {
                lines.add(line);
                count++;
            }

            String csvSample = String.join("\\n", lines);

            String prompt = "You are a fraud detection expert. Analyze the following transaction records and identify any suspicious or fraudulent transactions. "
                    + "Look for patterns such as:\\n"
                    + "- Repeated credits of the same amount from different source accounts\\n"
                    + "- Unusually large transactions\\n"
                    + "- Multiple transactions from the same account in a short time\\n"
                    + "- Any other anomalies\\n\\n"
                    + "Return a list of customer id's and the suspicious transaction reason which has suspicious transactions .\\n\\n"
                    + "Here is the data:\\n" + csvSample;

            // Prepare request
            Map<String, Object> systemMessage = Map.of("role", "system", "content", "You are a helpful fraud detection assistant.");
            Map<String, Object> userMessage = Map.of("role", "user", "content", prompt);

            Map<String, Object> requestBody = Map.of(
                    "messages", List.of(systemMessage, userMessage),
                    "max_tokens", 4096,
                    "temperature", 1.0,
                    "top_p", 1.0
            );

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
        return null;
    }

    public ResponseEntity<String> detectFraudFromCsv(String query) {
        try {
            // Define a semantic query for fraud detection
            String newQuery = "Find suspicious or fraudulent transactions such as repeated credits, large amounts, or rapid transactions";

            SearchRequest searchRequest = SearchRequest.builder()
                    .query(newQuery)
                    .build();

            List<Document> results = vectorStore.similaritySearch(searchRequest);

            if (results == null || results.isEmpty()) {
                return ResponseEntity.ok("No suspicious transactions found.");
            }

            StringBuilder sb = new StringBuilder("🔍 Suspicious Transactions Found:\n\n");
            for (Document doc : results) {
                sb.append("- ").append(doc.getFormattedContent()).append("\n");
            }

            return ResponseEntity.ok(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error during fraud detection: " + e.getMessage());
        }
    }


    public List<Transaction> getTransactionData() throws IOException {

        try {
            Resource resource = new DefaultResourceLoader().getResource("classpath:/static/dummy_transaction_data.csv");

            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));


            List<Transaction> transactions = new ArrayList<>();

            String line;
            boolean isFirstLine = true;
             while ((line = reader.readLine()) != null) {
                 if (isFirstLine) {
                 isFirstLine = false; // Skip header
                 continue;
                 }
             String[] fields = line.split(",");
                 Transaction transaction = new Transaction();
                 transaction.setCustomerId(fields[0]);
                 transaction.setAccountId(fields[1]);
                 transaction.setTransactionDate(fields[2]);
                 transaction.setTransactionAmount(Double.parseDouble(fields[3]));
                 transaction.setCurrency(fields[4]);
                 transaction.setTransactionType(fields[5]);
                 transaction.setSourceAccount(fields[6]);
                 transaction.setIsFraud(Integer.parseInt(fields[7]));
                 transactions.add(transaction);
                 }
            System.out.println("✅ Loaded " + transactions.size() + " transactions into the vector store.");
            return transactions;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Transaction getTransaction(String[] parts) {
        Transaction transaction = new Transaction();
        transaction.setCustomerId(parts[0]);
        transaction.setAccountId(parts[1]);
        transaction.setTransactionDate(parts[2]);
        transaction.setTransactionAmount(Double.parseDouble(parts[3]));
        transaction.setCurrency(parts[4]);
        transaction.setTransactionType(parts[5]);
        transaction.setSourceAccount(parts[6]);
        transaction.setIsFraud(Integer.parseInt(parts[7]));

        String content = String.format(
                "Customer %s made a %s of %.2f %s on %s from account %s to %s. Fraud status: %s.",
                transaction.getCustomerId(), transaction.getTransactionType(), transaction.getTransactionAmount(),
                transaction.getCurrency(), transaction.getTransactionDate(), transaction.getSourceAccount(),
                transaction.getAccountId(), transaction.getIsFraud() == 1 ? "Yes" : "No"
        );
        return transaction;
    }



//    public ResponseEntity<String> getFraudTransactions(List<Transaction> transactions) {
//
//        try {
//            // Build CSV-like sample from transaction list
//            StringBuilder csvSampleBuilder = new StringBuilder();
//            csvSampleBuilder.append("customer_id,account_id,transaction_amount,source_account,transaction_date,is_fraud\n");
//
//            int count = 0;
//            for (Transaction tx : transactions) {
//                if (count++ >= 10) break; // Limit to first 10 for prompt
//                csvSampleBuilder.append(String.format("%s,%s,%.2f,%s,%s,%d\n",
//                        tx.getCustomerId(),
//                        tx.getAccountId(),
//                        tx.getTransactionAmount(),
//                        tx.getSourceAccount(),
//                        tx.getTransactionDate(),
//                        tx.getIsFraud()));
//            }
//
//            String csvSample = csvSampleBuilder.toString();
//
//            String prompt = "You are a fraud detection expert. Analyze the following transaction records and identify any suspicious or fraudulent transactions. "
//                    + "Look for patterns such as:\n"
//                    + "- Repeated credits of the same amount from different source accounts\n"
//                    + "- Unusually large transactions\n"
//                    + "- Multiple transactions from the same account in a short time\n"
//                    + "- Any other anomalies\n\n"
//                    + "Return a list of customer IDs and the suspicious transaction reason for each.\n\n"
//                    + "Here is the data:\n" + csvSample;
//
//            // Prepare request
//            Map<String, Object> systemMessage = Map.of("role", "system", "content", "You are a helpful fraud detection assistant.");
//            Map<String, Object> userMessage = Map.of("role", "user", "content", prompt);
//
//            Map<String, Object> requestBody = Map.of(
//                    "messages", List.of(systemMessage, userMessage),
//                    "max_tokens", 4096,
//                    "temperature", 1.0,
//                    "top_p", 1.0
//            );
//
//
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("api-key", API_KEY);
//
//            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
//
//            RestTemplate restTemplate = new RestTemplate();
//            ResponseEntity<Map> response = restTemplate.postForEntity(ENDPOINT, request, Map.class);
//
//            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
//                if (choices != null && !choices.isEmpty()) {
//                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
//                    if (message != null && message.containsKey("content")) {
//                        return ResponseEntity.ok((String) message.get("content"));
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
//        }
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No response from model.");
//    }


}