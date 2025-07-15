package com.example.fraud.entity;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;
import lombok.Data;

@Data
@Container(containerName = "Transactions")
public class Transaction {

    @Id
    private String transactionId;

    private String customerId;
    @PartitionKey
    private String accountId;
    private String transaction_date;
    private double transaction_amount;
    private String currency;
    private String transaction_type;
    private String source_account;
    private int is_fraud;
}
