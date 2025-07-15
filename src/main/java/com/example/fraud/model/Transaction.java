package com.example.fraud.model;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class Transaction {

    private String customerId;
    private String accountId;
    private String transactionDate;
    private double transactionAmount;
    private String currency;
    private String transactionType;
    private String sourceAccount;
    private int isFraud;


    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
