package com.example.hhvolgograd.mail.service;

import com.example.hhvolgograd.configuration.ProjectMailProperty;
import com.example.hhvolgograd.mail.model.SimpleMailMessageBuilder;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@AllArgsConstructor
public class MailServiceImpl  implements MailService {

    private final JavaMailSender sender;
    private final ProjectMailProperty mailProperty;

    @Override
    public void send(String userAddress, String text) {
        val message = SimpleMailMessageBuilder.create()
                .from(mailProperty.getFrom())
                .to(userAddress)
                .sentDate(new Date())
                .subject("Registration confirmation")
                .text(text)
                .build();

        sender.send(message);
    }
}
