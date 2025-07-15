package com.example.fraud.service;

import com.example.fraud.entity.Transaction;
import com.example.fraud.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionDataService {


    private final TransactionRepository transactionRepository;

    public TransactionDataService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<com.example.fraud.model.Transaction> getAllTransactions() {
        return mapModel(transactionRepository.findAll());
    }

    public List<Transaction> getTransactionsByCustomer(String customerId) {
        return transactionRepository.findByCustomerId(customerId);
    }

        public List<com.example.fraud.model.Transaction> mapModel(List<Transaction> transactionList) {
            List<Transaction> entities = transactionList;
            List<com.example.fraud.model.Transaction> modelList = new ArrayList<>();
            for (Transaction entity: entities){
                com.example.fraud.model.Transaction model = new com.example.fraud.model.Transaction();
                model.setCustomerId(entity.getCustomerId());
                model.setAccountId(entity.getAccountId());
                model.setTransactionDate(entity.getTransaction_date());
                model.setTransactionAmount(entity.getTransaction_amount());
                model.setCurrency(entity.getCurrency());
                model.setTransactionType(entity.getTransaction_type());
                model.setSourceAccount(entity.getSource_account());
                model.setTransactionId(entity.getTransactionId());
                modelList.add(model);
            }

            return modelList;
        }




}
