package com.example.fraud.controller;

import com.example.fraud.service.FraudDetectClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client")
public class FraudTransactionClientController {

    @Autowired
    FraudDetectClientService fraudDetectService;

    @PostMapping("/findFraudTransactions")
    public ResponseEntity<String> findFraudTransactions(@RequestBody String prompt) {
        // You can enhance this to parse intent from prompt
        if (prompt.toLowerCase().contains("fraud")) {
            String result = String.valueOf(fraudDetectService.detectFraudFromMcpServer());
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body("Sorry, I don't understand the request.");
    }
}

