package com.example.fraud.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerNotification {

    private String customerId;
    private EmailContent emailContent;
    private String fraudReason;
}
