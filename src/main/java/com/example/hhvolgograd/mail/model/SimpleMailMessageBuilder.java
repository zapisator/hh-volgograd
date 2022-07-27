package com.example.hhvolgograd.mail.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.mail.SimpleMailMessage;

import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SimpleMailMessageBuilder {

    private String from;
    private String replyTo;
    private String[] to;
    private String[] cc;
    private String[] bcc;
    private Date sentDate;
    private String subject;
    private String text;

    public static SimpleMailMessageBuilder create() {
        return new SimpleMailMessageBuilder();
    }

    public SimpleMailMessageBuilder from(String from) {
        this.from = from;
        return this;
    }

    public SimpleMailMessageBuilder replyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }
    public SimpleMailMessageBuilder to(String... to) {
        this.to = to;
        return this;
    }
    public SimpleMailMessageBuilder cc(String... cc) {
        this.cc = cc;
        return this;
    }
    public SimpleMailMessageBuilder bcc(String... bcc) {
        this.bcc = bcc;
        return this;
    }
    public SimpleMailMessageBuilder sentDate(Date sentDate) {
        this.sentDate = sentDate;
        return this;
    }
    public SimpleMailMessageBuilder subject(String subject) {
        this.subject = subject;
        return this;
    }
    public SimpleMailMessageBuilder text(String text) {
        this.text = text;
        return this;
    }

    public SimpleMailMessage build() {
        val message = new SimpleMailMessage();

        message.setFrom(from);
        message.setReplyTo(replyTo);
        message.setTo(to);
        message.setCc(cc);
        message.setBcc(bcc);
        message.setSentDate(sentDate);
        message.setSubject(subject);
        message.setText(text);
        return message;
    }

}
