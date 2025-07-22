package com.example.fraud.controller;

import com.example.fraud.model.EmailContent;
import com.example.fraud.service.AgentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

//package com.example.fraud.controller;
//
//
//import com.example.fraud.service.FraudDetectClientService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/agent")
//public class FraudAgentController {
//
//    @Autowired
//    FraudDetectClientService fraudDetectService;
//
//    @PostMapping("/findFraudTransactions")
//    public ResponseEntity<String> askAgent(@RequestBody String prompt) {
//     // You can enhance this to parse intent from prompt
//         if (prompt.toLowerCase().contains("fraud")) {
//         String result = String.valueOf(fraudDetectService.detectFraudFromMcpServer());
//         return ResponseEntity.ok(result);
//         }
//     return ResponseEntity.badRequest().body("Sorry, I don't understand the request.");
//     }
//}
@RestController
@RequestMapping("/agent")
public class AgentController {

    @Autowired
    AgentService agentService;

    @PostMapping("/invokeFraudDetection")
    public ResponseEntity<String> invokeFraudDetectClient(@RequestBody String prompt) {
        return agentService.fraudClientServiceTransactions(prompt);
    }

    @PostMapping("/invokeEmailClient")
    public ResponseEntity<String> invokeEmailClient(@RequestBody EmailContent EmailContent) {
//        return ResponseEntity.ok("U r good");
        return agentService.emailClientNotification(EmailContent);
    }

    @PostMapping("/sendNotification")
    public ResponseEntity<String> sendCustomerNotification(@RequestBody String prompt) throws JsonProcessingException {
//      String response="<200 OK OK,[{\"customerId\":\"9999\",\"transactions\":[{\"transaction_date\":\"2023-01-01\",\"transaction_amount\":123.45},{\"transaction_date\":\"2023-01-02\",\"transaction_amount\":123.45},{\"transaction_date\":\"2023-01-03\",\"transaction_amount\":123.45},{\"transaction_date\":\"2023-01-04\",\"transaction_amount\":123.45},{\"transaction_date\":\"2023-01-05\",\"transaction_amount\":123.45},{\"transaction_date\":\"2023-01-06\",\"transaction_amount\":123.45},{\"transaction_date\":\"2023-01-07\",\"transaction_amount\":123.45},{\"transaction_date\":\"2023-01-08\",\"transaction_amount\":123.45},{\"transaction_date\":\"2023-01-09\",\"transaction_amount\":123.45},{\"transaction_date\":\"2023-01-10\",\"transaction_amount\":123.45}],\"reason\":\"Repeated credits of the same amount (123.45) from different source accounts over consecutive days.\"}],[]>";
        ResponseEntity<String> response = agentService.fraudClientServiceTransactions(prompt);
        System.out.println("response-->"+response);
        return agentService.sendNotification(response.toString());
    }
}
