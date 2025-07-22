package com.example.fraud.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailContent {

    private String recipientId;
    private String subject;
    private String body;
}
