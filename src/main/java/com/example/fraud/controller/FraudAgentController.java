package com.example.fraud.controller;


import com.example.fraud.service.FraudDetectClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent")
public class FraudAgentController {

    @Autowired
    FraudDetectClientService fraudDetectService;

    @PostMapping("/ask")
    public ResponseEntity<String> askAgent(@RequestBody String prompt) {
     // You can enhance this to parse intent from prompt
         if (prompt.toLowerCase().contains("fraud")) {
         String result = String.valueOf(fraudDetectService.detectFraudFromMcpServer());
         return ResponseEntity.ok(result);
         }
     return ResponseEntity.badRequest().body("Sorry, I don't understand the request.");
     }


}
