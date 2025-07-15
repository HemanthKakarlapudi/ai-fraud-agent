package com.example.fraud.controller;

import com.example.fraud.model.Transaction;
import com.example.fraud.service.TransactionDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/server/fraud")
public class FraudDetectServerController {

    @Autowired
    TransactionDataService transactionDataService;

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions() throws IOException {
//        return ResponseEntity.ok(fraudDetectService.getTransactionData());
        return ResponseEntity.ok(transactionDataService.getAllTransactions());
    }

}
