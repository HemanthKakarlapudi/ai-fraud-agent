package com.example.fraud.service;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailAddress;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.example.fraud.model.EmailContent;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    String connectionString = "";
    String SENDER_ADDRESS = "";

    EmailClient emailClient = new EmailClientBuilder()
            .connectionString(connectionString)
            .buildClient();

    public PollResponse<EmailSendResult> sendEmail(EmailContent emailContent) {
        EmailAddress toAddress1 = new EmailAddress(emailContent.getRecipientId())
                .setDisplayName("Hemanth");

        EmailMessage message = new EmailMessage()
                .setSenderAddress(SENDER_ADDRESS)
                .setSubject(emailContent.getSubject())
                .setBodyPlainText(emailContent.getBody())
                .setToRecipients(toAddress1);

        SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
        PollResponse<EmailSendResult> response = poller.waitForCompletion();

        System.out.println("Operation Id: " + response.getValue().getId());
        return response;
    }
}
