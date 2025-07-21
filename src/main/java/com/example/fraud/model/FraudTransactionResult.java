package com.example.fraud.model;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FraudTransactionResult {
    private String customerId;
    private List<Transaction> transactions;
    private String reason;

    // Getters and setters
}
