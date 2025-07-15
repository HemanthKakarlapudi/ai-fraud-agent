package com.example.fraud.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.example.fraud.entity.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends CosmosRepository<Transaction, String> {
    List<Transaction> findByCustomerId(String customerId);

    List<Transaction> findAll();
}
