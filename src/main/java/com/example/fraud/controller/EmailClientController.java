package com.example.fraud.controller;

import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.PollResponse;
import com.example.fraud.model.EmailContent;
import com.example.fraud.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client")
public class EmailClientController {

    @Autowired
    EmailService emailService;

    @PostMapping("/sendEmailNotification")
    public PollResponse<EmailSendResult> sendCustomerEmailNotification(@RequestBody EmailContent EmailContent) {
        return emailService.sendEmail(EmailContent);
    }
}
